package jp.ac.titech.cs.se.miner2hub.converter

import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.models.Refactoring

data class RefactoringOutput(
    val type: String,
    val description: String,
    val commit: Commit,
    val data: Refactoring.Data
)