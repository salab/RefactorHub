package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.autofill
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.RefactoringType
import kotlin.reflect.full.createInstance

fun Refactoring.putCodeElementHolder(
    category: DiffCategory,
    key: String,
    typeName: String,
    multiple: Boolean
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        val type = try {
            CodeElementType.valueOf(typeName)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("CodeElementType($typeName) is unsupported")
        }
        if (!map.containsKey(key)) {
            map[key] = CodeElementHolder(
                type,
                multiple,
                if (multiple) mutableListOf() else mutableListOf(type.klass.createInstance())
            )
        } else {
            throw RuntimeException("already has key=$key")
        }
    }
}

fun Refactoring.removeCodeElementHolder(
    category: DiffCategory,
    key: String,
    type: RefactoringType
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(key)) {
            throw RuntimeException("doesn't have key=$key")
        }
        val metadataMap = type.getCodeElementMetadataMap(category)
        if (metadataMap.containsKey(key)) {
            throw RuntimeException("should have key=$key")
        }
        map.remove(key)
    }
}

fun Refactoring.verifyCodeElementHolder(
    category: DiffCategory,
    key: String,
    state: Boolean
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(key)) {
            throw RuntimeException("doesn't have key=$key")
        }
        map[key]?.let { holder ->
            map[key] = holder.copy(state = if (state) CodeElementHolder.State.Manual else CodeElementHolder.State.None)
        }
    }
}

fun Refactoring.appendCodeElementDefaultValue(
    category: DiffCategory,
    key: String
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(key)) {
            throw RuntimeException("doesn't have key=$key")
        }
        map[key]?.let { holder ->
            if (!holder.multiple) {
                throw RuntimeException("key=$key doesn't have multiple elements")
            }
            map[key] = holder.copy(
                elements = holder.elements.toMutableList().also { it.add(holder.type.klass.createInstance()) }
            )
        }
    }
}

fun Refactoring.updateCodeElementValue(
    category: DiffCategory,
    key: String,
    index: Int,
    element: CodeElement,
    type: RefactoringType,
    content: CommitContent
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(key)) {
            throw RuntimeException("doesn't have key=$key")
        }
        map[key]?.let { holder ->
            try {
                map[key] = holder.copy(
                    elements = holder.elements.toMutableList().also { it[index] = element },
                    state = CodeElementHolder.State.Manual
                )
            } catch (e: IndexOutOfBoundsException) {
                throw RuntimeException("key=$key doesn't have index=$index")
            }
        }
        this.data.before.processAutofill(category, key, element, DiffCategory.before, type, content)
        this.data.after.processAutofill(category, key, element, DiffCategory.after, type, content)
    }
}

private fun MutableMap<String, CodeElementHolder>.processAutofill(
    category: DiffCategory,
    key: String,
    element: CodeElement,
    target: DiffCategory,
    type: RefactoringType,
    content: CommitContent
) {
    type.getCodeElementMetadataMap(target).entries.forEach metadata@{
        it.value.autofills.forEach { autofill ->
            autofill.follows.forEach { follow ->
                if (follow.category == category && follow.name == key) {
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

fun Refactoring.removeCodeElementValue(
    category: DiffCategory,
    key: String,
    index: Int
): Refactoring {
    return this.copy().apply {
        val map = getCodeElementHolderMap(category)
        if (!map.containsKey(key)) {
            throw RuntimeException("doesn't have key=$key")
        }
        map[key]?.let { holder ->
            if (!holder.multiple) {
                map[key] = CodeElementHolder(
                    holder.type,
                    holder.multiple,
                    mutableListOf(holder.type.klass.createInstance())
                )
            } else {
                try {
                    map[key] = holder.copy(elements = holder.elements.toMutableList().also { it.removeAt(index) })
                } catch (e: IndexOutOfBoundsException) {
                    throw RuntimeException("key=$key doesn't have index=$index")
                }
            }
        }
    }
}

private fun Refactoring.getCodeElementHolderMap(category: DiffCategory): MutableMap<String, CodeElementHolder> {
    return when (category) {
        DiffCategory.before -> this.data.before
        DiffCategory.after -> this.data.after
    }
}

private fun RefactoringType.getCodeElementMetadataMap(
    category: DiffCategory
): Map<String, CodeElementMetadata> {
    return when (category) {
        DiffCategory.before -> this.before
        DiffCategory.after -> this.after
    }
}
