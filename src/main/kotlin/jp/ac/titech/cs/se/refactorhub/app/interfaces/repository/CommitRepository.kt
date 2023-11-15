package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.ChangedFile
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import java.time.LocalDateTime
import java.util.UUID

interface CommitRepository {
    fun find(owner: String, repository: String, sha: String): Commit?
    fun findById(id: UUID): Commit?
    fun findAll(): List<Commit>

    fun create(
        owner: String,
        repository: String,
        sha: String,
        parentSha: String,
        url: String,
        message: String,
        authorName: String,
        authoredDateTime: LocalDateTime,
        beforeFiles: List<File>,
        afterFiles: List<ChangedFile>,
        diffHunks: List<DiffHunk>,
        patch: String
    ): Commit
}
