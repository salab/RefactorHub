package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.ChangedFile
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class CommitRepositoryImpl : CommitRepository {
    override fun find(owner: String, repository: String, sha: String): Commit? {
        return transaction {
            CommitDao.find {
                (Commits.owner eq owner) and
                    (Commits.repository eq repository) and
                    (Commits.sha eq sha)
            }.firstOrNull()?.asModel()
        }
    }

    override fun findById(id: UUID): Commit? {
        return transaction {
            CommitDao.findById(id)?.asModel()
        }
    }

    override fun findAll(): List<Commit> {
        return transaction {
            CommitDao.all().map { it.asModel() }
        }
    }

    override fun create(
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
    ): Commit {
        return transaction {
            CommitDao.new {
                this.owner = owner
                this.repository = repository
                this.sha = sha
                this.parentSha = parentSha
                this.url = url
                this.message = message
                this.authorName = authorName
                this.authoredDateTime = authoredDateTime.toString()
                this.beforeFiles = beforeFiles
                this.afterFiles = afterFiles
                this.diffHunks = diffHunks
                this.patch = patch
            }.asModel()
        }
    }
}
