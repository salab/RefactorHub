package jp.ac.titech.cs.se.refactorhub.tool.model.editor

import jp.ac.titech.cs.se.refactorhub.tool.model.DiffCategory

data class CommitFileContents(
    val owner: String,
    val repository: String,
    val sha: String,
    val files: Files
) {
    data class Files(
        val before: List<File>,
        val after: List<File>
    ) {
        fun get(category: DiffCategory) = if (category == DiffCategory.before) before else after
    }

    data class File(
        val name: String,
        val content: FileContent
    )
}
