package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
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

    override fun create(
        experimentId: UUID,
        orderIndex: Int,
        owner: String,
        repository: String,
        sha: String,
        parentSha: String,
        url: String,
        message: String,
        authorName: String,
        authoredDateTime: String,
        beforeFiles: List<File>,
        afterFiles: List<File>,
        fileMappings: List<FileMapping>,
        patch: String
    ): Commit {
        return transaction {
            CommitDao.new {
                this.experiment = ExperimentDao[experimentId]
                this.orderIndex = orderIndex
                this.owner = owner
                this.repository = repository
                this.sha = sha
                this.parentSha = parentSha
                this.url = url
                this.message = message
                this.authorName = authorName
                this.authoredDateTime = authoredDateTime
                this.beforeFiles = beforeFiles
                this.afterFiles = afterFiles
                this.fileMappings = fileMappings
                this.patch = patch
            }.asModel()
        }
    }
}
