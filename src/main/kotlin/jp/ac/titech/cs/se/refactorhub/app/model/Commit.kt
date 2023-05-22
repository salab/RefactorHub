package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import java.util.Date

data class Commit(
    override val owner: String,
    override val repository: String,
    override val sha: String
) : Commit

data class CommitDetail(
    val owner: String,
    val repository: String,
    val sha: String,
    val url: String,
    val message: String,
    val author: String,
    val authorDate: Date,
    val files: List<CommitFile>,
    val parent: String
)

data class CommitFile(
    val sha: String,
    val status: CommitFileStatus,
    val name: String,
    val previousName: String,
    val patch: String,
    val diffHunks: List<DiffHunk>
)

@Suppress("EnumEntryName")
enum class CommitFileStatus {
    modified, added, removed, renamed
}
