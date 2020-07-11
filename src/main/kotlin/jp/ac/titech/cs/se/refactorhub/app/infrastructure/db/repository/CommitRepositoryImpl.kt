package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import org.jetbrains.exposed.sql.transactions.transaction

class CommitRepositoryImpl : CommitRepository {
    override fun findBySha(sha: String): Commit? {
        return transaction {
            CommitDao.find {
                Commits.sha eq sha
            }.singleOrNull()?.let {
                Commit(it.sha, it.owner, it.repository)
            }
        }
    }

    override fun save(commit: Commit): Commit {
        return transaction {
            CommitDao.new {
                this.sha = commit.sha
                this.owner = commit.owner
                this.repository = commit.repository
            }.let {
                Commit(it.sha, it.owner, it.repository)
            }
        }
    }
}
