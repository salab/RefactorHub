package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

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

    override fun save(commit: Commit): Commit {
        return transaction {
            CommitDao.new {
                this.sha = commit.sha
                this.owner = commit.owner
                this.repository = commit.repository
            }.asModel()
        }
    }
}
