package jp.ac.titech.cs.se.refactorhub.tool.model.refactoring

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder

data class Refactoring(
    val type: String,
    val commit: Commit,
    val data: Data = Data(),
    val description: String = ""
) {
    data class Data(
        val before: Map<String, CodeElementHolder> = mapOf(),
        val after: Map<String, CodeElementHolder> = mapOf()
    )
}
