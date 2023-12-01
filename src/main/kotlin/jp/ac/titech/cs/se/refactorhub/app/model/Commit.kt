package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import jp.ac.titech.cs.se.refactorhub.core.model.divider.CommitFileData
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
    override val beforeFiles: List<File>,
    override val afterFiles: List<File>,
    override val fileMappings: List<FileMapping>,
    override val patch: String
) : Commit, CommitFileData
