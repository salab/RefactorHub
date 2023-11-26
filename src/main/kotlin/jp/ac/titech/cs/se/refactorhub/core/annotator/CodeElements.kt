package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.autofill
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.change.Change
import jp.ac.titech.cs.se.refactorhub.core.model.change.ChangeType
import kotlin.reflect.full.createInstance

fun Change.putCodeElementHolder(
    category: DiffCategory,
    parameterName: String,
    codeElementTypeName: String,
    multiple: Boolean
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        val type = try {
            CodeElementType.valueOf(codeElementTypeName)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("CodeElementType($codeElementTypeName) is unsupported")
        }
        if (!map.containsKey(parameterName)) {
            map[parameterName] = CodeElementHolder(
                type,
                multiple,
                if (multiple) mutableListOf() else mutableListOf(type.klass.createInstance())
            )
        } else {
            throw RuntimeException("already has key=$parameterName")
        }
    }
}

fun Change.removeCodeElementHolder(
    category: DiffCategory,
    parameterName: String,
    changeType: ChangeType
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(parameterName)) {
            throw RuntimeException("doesn't have key=$parameterName")
        }
        val metadataMap = changeType.getCodeElementMetadataMap(category)
        if (metadataMap.containsKey(parameterName)) {
            throw RuntimeException("should have key=$parameterName")
        }
        map.remove(parameterName)
    }
}

fun Change.verifyCodeElementHolder(
    category: DiffCategory,
    parameterName: String,
    isVerified: Boolean
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(parameterName)) {
            throw RuntimeException("doesn't have key=$parameterName")
        }
        map[parameterName]?.let { holder ->
            map[parameterName] = holder.copy(state = if (isVerified) CodeElementHolder.State.Manual else CodeElementHolder.State.None)
        }
    }
}

fun Change.appendCodeElementDefaultValue(
    category: DiffCategory,
    parameterName: String
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(parameterName)) {
            throw RuntimeException("doesn't have key=$parameterName")
        }
        map[parameterName]?.let { holder ->
            if (!holder.multiple) {
                throw RuntimeException("key=$parameterName doesn't have multiple elements")
            }
            map[parameterName] = holder.copy(
                elements = holder.elements.toMutableList().also { it.add(holder.type.klass.createInstance()) }
            )
        }
    }
}

fun Change.updateCodeElementValue(
    category: DiffCategory,
    parameterName: String,
    elementIndex: Int,
    element: CodeElement,
    changeType: ChangeType,
    content: CommitContent
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(parameterName)) {
            throw RuntimeException("doesn't have key=$parameterName")
        }
        map[parameterName]?.let { holder ->
            try {
                map[parameterName] = holder.copy(
                    elements = holder.elements.toMutableList().also { it[elementIndex] = element },
                    state = CodeElementHolder.State.Manual
                )
            } catch (e: IndexOutOfBoundsException) {
                throw RuntimeException("key=$parameterName doesn't have index=$elementIndex")
            }
        }
        this.parameterData.before.processAutofill(category, parameterName, element, DiffCategory.before, changeType, content)
        this.parameterData.after.processAutofill(category, parameterName, element, DiffCategory.after, changeType, content)
    }
}

private fun MutableMap<String, CodeElementHolder>.processAutofill(
    category: DiffCategory,
    parameterName: String,
    element: CodeElement,
    target: DiffCategory,
    changeType: ChangeType,
    content: CommitContent
) {
    changeType.getCodeElementMetadataMap(target).entries.forEach metadata@{
        it.value.autofills.forEach { autofill ->
            autofill.follows.forEach { follow ->
                if (follow.category == category && follow.name == parameterName) {
                    val holder = this[it.key]
                    if (holder != null && holder.state == CodeElementHolder.State.Manual) return@metadata
                    val elements = autofill(autofill, category, element, target, it.value, content)
                    this[it.key] = CodeElementHolder(
                        it.value.type,
                        it.value.multiple,
                        elements.toMutableList(),
                        CodeElementHolder.State.Autofill
                    )
                }
            }
        }
    }
}

fun Change.removeCodeElementValue(
    category: DiffCategory,
    parameterName: String,
    elementIndex: Int
): Change {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(parameterName)) {
            throw RuntimeException("doesn't have key=$parameterName")
        }
        map[parameterName]?.let { holder ->
            if (!holder.multiple) {
                map[parameterName] = CodeElementHolder(
                    holder.type,
                    holder.multiple,
                    mutableListOf(holder.type.klass.createInstance())
                )
            } else {
                try {
                    map[parameterName] = holder.copy(elements = holder.elements.toMutableList().also { it.removeAt(elementIndex) })
                } catch (e: IndexOutOfBoundsException) {
                    throw RuntimeException("key=$parameterName doesn't have index=$elementIndex")
                }
            }
        }
    }
}

private fun Change.getCodeElementHolderMap(category: DiffCategory): MutableMap<String, CodeElementHolder> {
    return when (category) {
        DiffCategory.before -> this.parameterData.before
        DiffCategory.after -> this.parameterData.after
    }
}

private fun ChangeType.getCodeElementMetadataMap(
    category: DiffCategory
): Map<String, CodeElementMetadata> {
    return when (category) {
        DiffCategory.before -> this.before
        DiffCategory.after -> this.after
    }
}
