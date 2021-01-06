package jp.ac.titech.cs.se.refactorhub.tool.model.editor

data class CommitFileContents(
    val owner: String,
    val repository: String,
    val sha: String,
    val files: List<File>
) {
    data class File(
        val name: String,
        val content: FileContent
    )
}
