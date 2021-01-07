package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import java.util.Date

data class Commit(
    override val sha: String,
    override val owner: String,
    override val repository: String
) : Commit

data class CommitDetail(
    val sha: String,
    val owner: String,
    val repository: String,
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
    val previousName: String
)

enum class CommitFileStatus {
    modified, added, removed, renamed
}
