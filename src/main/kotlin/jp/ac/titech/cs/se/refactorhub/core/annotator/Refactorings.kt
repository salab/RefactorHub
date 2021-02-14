package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.RefactoringType
import kotlin.reflect.full.createInstance

/**
 * Format Refactoring.Data into RefactoringType.
 * Override/Add holders that defined on type.
 */
fun Refactoring.Data.format(type: RefactoringType): Refactoring.Data {
    return this.copy().apply {
        before.putDefaultCodeElementHolders(type.before)
        after.putDefaultCodeElementHolders(type.before)
    }
}

internal fun Refactoring.copy(
    type: String = this.type,
    commit: Commit = this.commit,
    data: Refactoring.Data = this.data.copy(),
    description: String = this.description
): Refactoring {
    return object : Refactoring {
        override val type: String = type
        override val commit: Commit = commit
        override val data: Refactoring.Data = data
        override val description: String = description
    }
}

private fun Refactoring.Data.copy(): Refactoring.Data {
    return object : Refactoring.Data {
        override val before: MutableMap<String, CodeElementHolder> = this@copy.before.copy()
        override val after: MutableMap<String, CodeElementHolder> = this@copy.after.copy()
    }
}

private fun MutableMap<String, CodeElementHolder>.copy(): MutableMap<String, CodeElementHolder> {
    return this.map {
        it.key to it.value.let { holder ->
            holder.copy(elements = ArrayList(holder.elements))
        }
    }.toMap().toMutableMap()
}

/**
 * Change RefactoringType and format Refactoring.Data to RefactoringType
 */
fun Refactoring.changeType(type: RefactoringType): Refactoring {
    if (this.type != type.name) {
        return this.copy(
            type = type.name,
            data = this.data.copy().apply {
                before.removeEmptyCodeElementHolders(type.before)
                before.putDefaultCodeElementHolders(type.before)
                after.removeEmptyCodeElementHolders(type.after)
                after.putDefaultCodeElementHolders(type.after)
            }
        )
    }
    return this
}

private fun MutableMap<String, CodeElementHolder>.removeEmptyCodeElementHolders(
    metadataMap: Map<String, CodeElementMetadata>
) {
    metadataMap.entries.forEach {
        if (this[it.key]?.elements?.isEmpty() == true) this.remove(it.key)
    }
}

private fun MutableMap<String, CodeElementHolder>.putDefaultCodeElementHolders(
    metadataMap: Map<String, CodeElementMetadata>
) {
    metadataMap.entries.forEach {
        if (this.containsKey(it.key)) {
            this[it.key]?.let { holder ->
                if (it.value.type != holder.type || it.value.multiple != holder.multiple) {
                    this["${it.key} (conflict)"] = holder
                    this[it.key] = CodeElementHolder(
                        it.value.type,
                        it.value.multiple,
                        if (it.value.multiple) mutableListOf()
                        else mutableListOf(it.value.type.klass.createInstance())
                    )
                }
            }
        } else {
            this[it.key] = CodeElementHolder(
                it.value.type,
                it.value.multiple,
                if (it.value.multiple) mutableListOf()
                else mutableListOf(it.value.type.klass.createInstance())
            )
        }
    }
}
