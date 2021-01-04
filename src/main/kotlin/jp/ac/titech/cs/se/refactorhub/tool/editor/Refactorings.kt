package jp.ac.titech.cs.se.refactorhub.tool.editor

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.RefactoringType
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
                else listOf(holder.elements.singleOrNull() ?: it.value.type.klass.createInstance())
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
    category: String,
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
        map[key] = CodeElementHolder(type, multiple)
    } else {
        throw RuntimeException("already has key=$key")
    }
    return setCodeElementHolderMap(refactoring, category, map)
}

fun removeCodeElementKey(
    refactoring: Refactoring,
    type: RefactoringType,
    category: String,
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

fun appendCodeElementValue(
    refactoring: Refactoring,
    category: String,
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
        holder.elements.toMutableList().also { it.add(holder.type.klass.createInstance()) }
    )
    return setCodeElementHolderMap(refactoring, category, map)
}

fun updateCodeElementValue(
    refactoring: Refactoring,
    category: String,
    key: String,
    index: Int,
    element: CodeElement
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
            holder.elements.toMutableList().also { it[index] = element }
        )
    } catch (e: IndexOutOfBoundsException) {
        throw RuntimeException("key=$key doesn't have index=$index")
    }
    return setCodeElementHolderMap(refactoring, category, map)
}

fun removeCodeElementValue(
    refactoring: Refactoring,
    category: String,
    key: String,
    index: Int
): Refactoring {
    val map = getCodeElementHolderMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val holder = map[key]!!
    if (!holder.multiple) {
        throw RuntimeException("key=$key doesn't have multiple elements")
    }
    try {
        map[key] = CodeElementHolder(
            holder.type,
            holder.multiple,
            holder.elements.toMutableList().also { it.removeAt(index) }
        )
    } catch (e: IndexOutOfBoundsException) {
        throw RuntimeException("key=$key doesn't have index=$index")
    }
    return setCodeElementHolderMap(refactoring, category, map)
}

private fun getCodeElementHolderMap(
    refactoring: Refactoring,
    category: String
): Map<String, CodeElementHolder> {
    return when (category) {
        "before" -> refactoring.data.before
        "after" -> refactoring.data.after
        else -> throw RuntimeException("should be either 'before' or 'after' but category=$category")
    }
}

private fun getCodeElementMetadataMap(
    type: RefactoringType,
    category: String
): Map<String, CodeElementMetadata> {
    return when (category) {
        "before" -> type.before
        "after" -> type.after
        else -> throw RuntimeException("should be either 'before' or 'after' but category=$category")
    }
}

private fun setCodeElementHolderMap(
    refactoring: Refactoring,
    category: String,
    map: Map<String, CodeElementHolder>
): Refactoring {
    return object : Refactoring {
        override val type: String = refactoring.type
        override val commit: Commit = refactoring.commit
        override val data: Refactoring.Data = object : Refactoring.Data {
            override val before: Map<String, CodeElementHolder> =
                if (category == "before") map else refactoring.data.before
            override val after: Map<String, CodeElementHolder> =
                if (category == "after") map else refactoring.data.after
        }
        override val description: String = refactoring.description
    }
}
