package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.Refactoring

data class Refactoring(
    val id: Int,
    val ownerId: Int,
    val parentId: Int? = null,
    override val commit: Commit,
    override val type: String,
    override val data: Data = Data(),
    override val description: String = "",
    val isVerified: Boolean = false
) : Refactoring {
    data class Data(
        override val before: MutableMap<String, CodeElementHolder> = mutableMapOf(),
        override val after: MutableMap<String, CodeElementHolder> = mutableMapOf()
    ) : Refactoring.Data
}
