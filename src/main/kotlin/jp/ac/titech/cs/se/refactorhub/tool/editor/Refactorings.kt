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
 * Fix Refactoring.Data that conflicts with the definition of RefactoringType
 */
fun fixRefactoringData(type: RefactoringType, data: Refactoring.Data): Refactoring.Data {
    return object : Refactoring.Data {
        override val before: Map<String, CodeElementHolder> =
            fixCodeElementMap(type.before, data.before)
        override val after: Map<String, CodeElementHolder> =
            fixCodeElementMap(type.after, data.after)
    }
}

/**
 * Change RefactoringType and fix Refactoring.Data
 */
fun changeRefactoringType(type: RefactoringType, refactoring: Refactoring): Refactoring {
    return if (type.name != refactoring.type) {
        val data = object : Refactoring.Data {
            override val before: Map<String, CodeElementHolder> =
                refactoring.data.before.let {
                    removeEmptyCodeElements(type.before, it)
                }.let {
                    putDefaultCodeElements(type.before, it)
                }
            override val after: Map<String, CodeElementHolder> =
                refactoring.data.after.let {
                    removeEmptyCodeElements(type.after, it)
                }.let {
                    putDefaultCodeElements(type.after, it)
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

private fun fixCodeElementMap(
    metadataMap: Map<String, CodeElementMetadata>,
    elementMap: Map<String, CodeElementHolder>
): Map<String, CodeElementHolder> {
    val map = elementMap.toMutableMap()
    metadataMap.entries.forEach {
        if (elementMap.containsKey(it.key)) {
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
        if (elementMap[it.key]?.elements?.isEmpty() == true) map.remove(it.key)
    }
    return map
}

private fun removeEmptyCodeElements(
    metadataMap: Map<String, CodeElementMetadata>,
    elementMap: Map<String, CodeElementHolder>
): Map<String, CodeElementHolder> {
    val map = elementMap.toMutableMap()
    metadataMap.entries.forEach {
        if (map[it.key]?.elements?.isEmpty() == true) map.remove(it.key)
    }
    return map
}

private fun putDefaultCodeElements(
    metadataMap: Map<String, CodeElementMetadata>,
    elementMap: Map<String, CodeElementHolder>
): Map<String, CodeElementHolder> {
    val map = elementMap.toMutableMap()
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
    category: String,
    key: String,
    typeName: String,
    multiple: Boolean,
    refactoring: Refactoring
): Refactoring {
    val map = getCodeElementMap(refactoring, category).toMutableMap()
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
    return setCodeElementMap(refactoring, category, map)
}

fun removeCodeElementKey(category: String, key: String, refactoring: Refactoring, type: RefactoringType): Refactoring {
    val map = getCodeElementMap(refactoring, category).toMutableMap()
    if (!map.containsKey(key)) {
        throw RuntimeException("doesn't have key=$key")
    }
    val metadata = getMetadataMap(type, category)
    if (metadata.containsKey(key)) {
        throw RuntimeException("should have key=$key")
    }
    map.remove(key)
    return setCodeElementMap(refactoring, category, map)
}

fun appendCodeElementValue(category: String, key: String, refactoring: Refactoring): Refactoring {
    val map = getCodeElementMap(refactoring, category).toMutableMap()
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
    return setCodeElementMap(refactoring, category, map)
}

fun updateCodeElementValue(
    category: String,
    key: String,
    index: Int,
    element: CodeElement,
    refactoring: Refactoring
): Refactoring {
    val map = getCodeElementMap(refactoring, category).toMutableMap()
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
    return setCodeElementMap(refactoring, category, map)
}

fun removeCodeElementValue(
    category: String,
    key: String,
    index: Int,
    refactoring: Refactoring
): Refactoring {
    val map = getCodeElementMap(refactoring, category).toMutableMap()
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
    return setCodeElementMap(refactoring, category, map)
}

private fun getCodeElementMap(refactoring: Refactoring, category: String): Map<String, CodeElementHolder> {
    return when (category) {
        "before" -> refactoring.data.before
        "after" -> refactoring.data.after
        else -> throw RuntimeException("should be either 'before' or 'after' but category=$category")
    }
}

private fun getMetadataMap(type: RefactoringType, category: String): Map<String, CodeElementMetadata> {
    return when (category) {
        "before" -> type.before
        "after" -> type.after
        else -> throw RuntimeException("should be either 'before' or 'after' but category=$category")
    }
}

private fun setCodeElementMap(
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
