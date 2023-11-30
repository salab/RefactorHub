package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.change.Change
import java.util.UUID

data class Change(
    val id: UUID,
    override val typeName: String,
    override val description: String = "",
    override val parameterData: ParameterData = ParameterData()
) : Change {
    data class ParameterData(
        override val before: MutableMap<String, CodeElementHolder> = mutableMapOf(),
        override val after: MutableMap<String, CodeElementHolder> = mutableMapOf()
    ) : Change.ParameterData
}
