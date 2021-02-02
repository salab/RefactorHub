package jp.ac.titech.cs.se.refactorhub.core.model.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory

data class CommitFileContents(
    val commit: Commit,
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
        val content: FileContent,
        val patch: String
    )
}
