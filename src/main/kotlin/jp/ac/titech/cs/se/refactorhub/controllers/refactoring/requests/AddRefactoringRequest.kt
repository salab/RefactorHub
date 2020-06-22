package jp.ac.titech.cs.se.refactorhub.controllers.refactoring.requests

import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.models.Refactoring

data class AddRefactoringRequest(
    val type: String,
    val description: String,
    val commit: Commit,
    val data: Refactoring.Data
)
