package jp.ac.titech.cs.se.refactorhub.old.controllers.refactoring.requests

import jp.ac.titech.cs.se.refactorhub.old.models.Commit
import jp.ac.titech.cs.se.refactorhub.old.models.Refactoring

data class AddRefactoringRequest(
    val type: String,
    val description: String,
    val commit: Commit,
    val data: Refactoring.Data
)
