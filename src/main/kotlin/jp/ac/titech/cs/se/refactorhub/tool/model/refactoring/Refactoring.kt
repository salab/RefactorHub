package jp.ac.titech.cs.se.refactorhub.tool.model.refactoring

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder

data class Refactoring(
    val type: String,
    val description: String,
    val commit: Commit,
    val data: Data
) {
    data class Data(
        val before: Map<String, CodeElementHolder>,
        val after: Map<String, CodeElementHolder>
    )
}
