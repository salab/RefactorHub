package jp.ac.titech.cs.se.refactorhub.core.model.change

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder

interface Change {
    val type: ChangeType
    val description: String
    val parameterData: ParameterData

    interface ParameterData {
        val before: MutableMap<String, CodeElementHolder>
        val after: MutableMap<String, CodeElementHolder>
    }
}
