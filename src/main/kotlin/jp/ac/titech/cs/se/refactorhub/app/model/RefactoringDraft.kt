package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring

data class RefactoringDraft(
    val type: String,
    val commit: Commit,
    val data: Refactoring.Data = Refactoring.Data(),
    val description: String = ""
)
