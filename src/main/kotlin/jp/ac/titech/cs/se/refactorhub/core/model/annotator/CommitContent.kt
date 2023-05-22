package jp.ac.titech.cs.se.refactorhub.core.model.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory

data class CommitContent(
    val commit: Commit,
    val files: List<FilePair>
) {
    data class FilePair(
        val before: File,
        val after: File,
        val patch: String,
        val diffHunks: List<DiffHunk>
    ) {
        fun get(category: DiffCategory) = if (category == DiffCategory.before) before else after
    }

    data class File(
        val name: String,
        val content: FileContent
    )
}

data class DiffHunk(
    val before: Hunk?,
    val after: Hunk?
) {
    data class Hunk(
        val startLine: Int,
        val endLine: Int
    )
}
