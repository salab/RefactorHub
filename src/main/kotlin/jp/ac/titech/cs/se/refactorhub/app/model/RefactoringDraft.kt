package jp.ac.titech.cs.se.refactorhub.app.model

data class RefactoringDraft(
    val id: Int,
    val ownerId: Int,
    val originId: Int,
    val isFork: Boolean,
    override val commit: Commit,
    override val type: String,
    override val data: Refactoring.Data = Refactoring.Data(),
    override val description: String = ""
) : jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
