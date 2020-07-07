package jp.ac.titech.cs.se.refactorhub.old.rminer.converter.refactoring

import jp.ac.titech.cs.se.refactorhub.old.models.Commit
import jp.ac.titech.cs.se.refactorhub.old.models.Refactoring

data class RefactoringOutput(
    val type: String,
    val description: String,
    val commit: Commit,
    val data: Refactoring.Data
)