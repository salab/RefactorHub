package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.change.Change
import jp.ac.titech.cs.se.refactorhub.core.model.change.ChangeType
import kotlin.reflect.full.createInstance

/**
 * Format Change.ParameterData into ChangeType.
 * Override/Add holders that defined on type.
 */
fun Change.ParameterData.format(type: ChangeType): Change.ParameterData {
    return this.copy().apply {
        before.putDefaultCodeElementHolders(type.before)
        after.putDefaultCodeElementHolders(type.after)
    }
}

internal fun Change.copy(
    typeName: String = this.typeName,
    description: String = this.description,
    parameterData: Change.ParameterData = this.parameterData.copy()
): Change {
    return object : Change {
        override val typeName: String = typeName
        override val description: String = description
        override val parameterData: Change.ParameterData = parameterData
    }
}

private fun Change.ParameterData.copy(): Change.ParameterData {
    return object : Change.ParameterData {
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
 * Change ChangeType and format Change.ParameterData to ChangeType
 */
fun Change.changeType(type: ChangeType): Change {
    if (this.typeName != type.name) {
        return this.copy(
            typeName = type.name,
            parameterData = this.parameterData.copy().apply {
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
    this.toMap().entries.forEach { (parameterName, element) ->
        val newElement = metadataMap[parameterName]
        if (newElement == null || newElement.type != element.type) {
            this.remove(parameterName)
        }
    }
}

private fun MutableMap<String, CodeElementHolder>.putDefaultCodeElementHolders(
    metadataMap: Map<String, CodeElementMetadata>
) {
    metadataMap.entries.forEach {
        if (!this.containsKey(it.key)) {
            this[it.key] = CodeElementHolder(
                it.value.type,
                it.value.multiple,
                if (it.value.multiple) mutableListOf()
                else mutableListOf(it.value.type.klass.createInstance())
            )
        }
    }
}
