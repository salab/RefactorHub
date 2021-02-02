package jp.ac.titech.cs.se.refactorhub.core.editor

import jp.ac.titech.cs.se.refactorhub.core.editor.autofill.autofill
import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.RefactoringType
import kotlin.reflect.full.createInstance

/**
 * Adjust Refactoring.Data to RefactoringType.
 * Override/Add holders that defined on type, remove empty holders that not defined on type.
 */
fun adjustRefactoringDataToType(data: Refactoring.Data, type: RefactoringType): Refactoring.Data {
    return object : Refactoring.Data {
        override val before: Map<String, CodeElementHolder> =
            adjustCodeElementHolderMapToType(data.before, type.before)
        override val after: Map<String, CodeElementHolder> =
            adjustCodeElementHolderMapToType(data.after, type.after)
    }
}

private fun adjustCodeElementHolderMapToType(
    holderMap: Map<String, CodeElementHolder>,
    metadataMap: Map<String, CodeElementMetadata>
): Map<String, CodeElementHolder> {
    val map = holderMap.toMutableMap()
    metadataMap.entries.forEach {
        if (holderMap.containsKey(it.key)) {
            val holder = map[it.key]!!
            map[it.key] = CodeElementHolder(
                holder.type,
                it.value.multiple,
                if (it.value.multiple) holder.elements
                else listOf(holder.elements.singleOrNull() ?: it.value.type.klass.createInstance()),
                holder.state
            )
        } else {
            map[it.key] = CodeElementHolder(
                it.value.type,
                it.value.multiple,
                if (it.value.multiple) listOf()
                else listOf(it.value.type.klass.createInstance())
            )
        }
        if (holderMap[it.key]?.elements?.isEmpty() == true) map.remove(it.key)
    }
    return map
}

/**
 * Change RefactoringType and adjust Refactoring.Data to RefactoringType
 */
fun changeRefactoringType(refactoring: Refactoring, type: RefactoringType): Refactoring {
    return if (type.name != refactoring.type) {
        val data = object : Refactoring.Data {
            override val before: Map<String, CodeElementHolder> =
                refactoring.data.before.let {
                    removeEmptyCodeElementHolders(it, type.before)
                }.let {
                    putDefaultCodeElementHolders(it, type.before)
                }
            override val after: Map<String, CodeElementHolder> =
                refactoring.data.after.let {
                    removeEmptyCodeElementHolders(it, type.after)
                }.let {
                    putDefaultCodeElementHolders(it, type.after)
                }
        }
        object : Refactoring {
            override val type: String = type.name
            override val commit: Commit = refactoring.commit
            override val data: Refactoring.Data = data
            override val description: String = refactoring.description
        }
    } else refactoring
}

private fun removeEmptyCodeElementHolders(
    holderMap: Map<String, CodeElementHolder>,
    metadataMap: Map<String, CodeElementMetadata>
): Map<String, CodeElementHolder> {
    val map = holderMap.toMutableMap()
    metadataMap.entries.forEach {
        if (map[it.key]?.elements?.isEmpty() == true) map.remove(it.key)
    }
    return map
}

private fun putDefaultCodeElementHolders(
    holderMap: Map<String, CodeElementHolder>,
    metadataMap: Map<String, CodeElementMetadata>
): Map<String, CodeElementHolder> {
    val map = holderMap.toMutableMap()
    metadataMap.entries.forEach {
        if (map[it.key]?.type != it.value.type) {
            map[it.key] = CodeElementHolder(
                it.value.type,
                it.value.multiple,
                if (it.value.multiple) listOf()
                else listOf(it.value.type.klass.createInstance())
            )
        }
    }
    return map
}

fun putCodeElementKey(
    refactoring: Refactoring,
    category: DiffCategory,
    key: String,
    typeName: String,
    multiple: Boolean
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    val type = try {
        CodeElementType.valueOf(typeName)
    } catch (e: IllegalArgumentException) {
        throw RuntimeException("CodeElementType(type=$typeName) is unsupported")
    }
    if (!map.containsKey(key)) {
        map[key] = CodeElementHolder(type, multiple, if (multiple) listOf() else listOf(type.klass.createInstance()))
    } else {
        throw RuntimeException("already has key=$key")
    }
    return setCodeElementHolderMap(refactoring, category, map)
}

fun removeCodeElementKey(
    refactoring: Refactoring,
    type: RefactoringType,
    category: DiffCategory,
    key: String
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val metadata = getCodeElementMetadataMap(type, category)
    if (metadata.containsKey(key)) {
        throw RuntimeException("should have key=$key")
    }
    map.remove(key)
    return setCodeElementHolderMap(refactoring, category, map)
}

fun verifyCodeElement(
    refactoring: Refactoring,
    category: DiffCategory,
    key: String,
    state: Boolean
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val holder = map[key]!!
    map[key] = CodeElementHolder(
        holder.type,
        holder.multiple,
        holder.elements,
        if (state) CodeElementHolder.State.Manual else CodeElementHolder.State.None
    )
    return setCodeElementHolderMap(refactoring, category, map)
}

fun appendCodeElementValue(
    refactoring: Refactoring,
    category: DiffCategory,
    key: String
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val holder = map[key]!!
    if (!holder.multiple) {
        throw RuntimeException("key=$key doesn't have multiple elements")
    }
    map[key] = CodeElementHolder(
        holder.type,
        holder.multiple,
        holder.elements.toMutableList().also { it.add(holder.type.klass.createInstance()) },
        holder.state
    )
    return setCodeElementHolderMap(refactoring, category, map)
}

fun updateCodeElementValue(
    refactoring: Refactoring,
    category: DiffCategory,
    key: String,
    index: Int,
    element: CodeElement,
    type: RefactoringType,
    contents: CommitFileContents
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val holder = map[key]!!
    try {
        map[key] = CodeElementHolder(
            holder.type,
            holder.multiple,
            holder.elements.toMutableList().also { it[index] = element },
            CodeElementHolder.State.Manual
        )
    } catch (e: IndexOutOfBoundsException) {
        throw RuntimeException("key=$key doesn't have index=$index")
    }
    return setCodeElementHolderMap(refactoring, category, map).let {
        processAutofill(DiffCategory.before, it, category, key, element, type, contents)
    }.let {
        processAutofill(DiffCategory.after, it, category, key, element, type, contents)
    }
}

private fun processAutofill(
    target: DiffCategory,
    refactoring: Refactoring,
    category: DiffCategory,
    key: String,
    element: CodeElement,
    type: RefactoringType,
    contents: CommitFileContents
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, target).toMutableMap()
    getCodeElementMetadataMap(type, target).entries.forEach {
        it.value.autofills.forEach { autofill ->
            autofill.follows.forEach(
                fun(follow) {
                    if (follow.category == category && follow.key == key) {
                        val holder = map[it.key]
                        if (holder != null && holder.state == CodeElementHolder.State.Manual) return
                        val elements = autofill(autofill, element, category, contents)
                        map[it.key] = CodeElementHolder(
                            it.value.type,
                            it.value.multiple,
                            elements,
                            CodeElementHolder.State.Autofill
                        )
                    }
                }
            )
        }
    }
    return setCodeElementHolderMap(refactoring, target, map)
}

fun removeCodeElementValue(
    refactoring: Refactoring,
    category: DiffCategory,
    key: String,
    index: Int
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val holder = map[key]!!
    if (!holder.multiple) {
        map[key] = CodeElementHolder(
            holder.type,
            holder.multiple,
            listOf(holder.type.klass.createInstance())
        )
    } else {
        try {
            map[key] = CodeElementHolder(
                holder.type,
                holder.multiple,
                holder.elements.toMutableList().also { it.removeAt(index) },
                holder.state
            )
        } catch (e: IndexOutOfBoundsException) {
            throw RuntimeException("key=$key doesn't have index=$index")
        }
    }
    return setCodeElementHolderMap(refactoring, category, map)
}

private fun getCodeElementHolderMap(
    refactoring: Refactoring,
    category: DiffCategory
): Map<String, CodeElementHolder> {
    return when (category) {
        DiffCategory.before -> refactoring.data.before
        DiffCategory.after -> refactoring.data.after
    }
}

private fun getCodeElementMetadataMap(
    type: RefactoringType,
    category: DiffCategory
): Map<String, CodeElementMetadata> {
    return when (category) {
        DiffCategory.before -> type.before
        DiffCategory.after -> type.after
    }
}

private fun setCodeElementHolderMap(
    refactoring: Refactoring,
    category: DiffCategory,
    map: Map<String, CodeElementHolder>
): Refactoring {
    return object : Refactoring {
        override val type: String = refactoring.type
        override val commit: Commit = refactoring.commit
        override val data: Refactoring.Data = object : Refactoring.Data {
            override val before: Map<String, CodeElementHolder> =
                if (category == DiffCategory.before) map else refactoring.data.before
            override val after: Map<String, CodeElementHolder> =
                if (category == DiffCategory.after) map else refactoring.data.after
        }
        override val description: String = refactoring.description
    }
}
