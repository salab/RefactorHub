package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring

data class Refactoring(
    val id: Int,
    val ownerId: Int,
    val parentId: Int? = null,
    override val commit: Commit,
    override val type: String,
    override val data: Data = Data(),
    override val description: String = ""
) : Refactoring {
    data class Data(
        override val before: Map<String, CodeElementHolder> = mapOf(),
        override val after: Map<String, CodeElementHolder> = mapOf()
    ) : Refactoring.Data
}
