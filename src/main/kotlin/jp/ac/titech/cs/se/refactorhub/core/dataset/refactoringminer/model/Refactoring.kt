package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder

data class Refactoring(
    val type: String,
    val commit: Commit,
    val data: Data = Data(),
    val description: String = ""
) {
    data class Data(
        val before: MutableMap<String, CodeElementHolder> = mutableMapOf(),
        val after: MutableMap<String, CodeElementHolder> = mutableMapOf()
    )
}
