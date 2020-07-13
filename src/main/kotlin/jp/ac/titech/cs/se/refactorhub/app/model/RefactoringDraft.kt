package jp.ac.titech.cs.se.refactorhub.app.model

data class RefactoringDraft(
    val id: Int,
    val ownerId: Int,
    val originId: Int,
    val isFork: Boolean,
    val commit: Commit,
    val type: String,
    val data: Refactoring.Data = Refactoring.Data(),
    val description: String = ""
)
