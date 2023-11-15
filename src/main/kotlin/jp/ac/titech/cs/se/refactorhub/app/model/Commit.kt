package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import java.time.LocalDateTime
import java.util.UUID

data class Commit(
    val id: UUID,
    override val owner: String,
    override val repository: String,
    override val sha: String,
    val parentSha: String,
    val url: String,
    val message: String,
    val authorName: String,
    val authoredDateTime: LocalDateTime,
    val beforeFiles: List<File>,
    val afterFiles: List<ChangedFile>,
    val diffHunks: List<DiffHunk>,
    val patch: String
) : Commit
