package jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.model

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring

data class Refactoring(
    override val type: String,
    override val commit: Commit,
    override val data: Data = Data(),
    override val description: String = ""
) : Refactoring {
    data class Data(
        override val before: Map<String, CodeElementHolder> = mapOf(),
        override val after: Map<String, CodeElementHolder> = mapOf()
    ) : Refactoring.Data
}
