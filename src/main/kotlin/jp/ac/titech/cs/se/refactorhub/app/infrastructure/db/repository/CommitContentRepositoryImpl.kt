package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContents
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitContentRepository
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import org.jetbrains.exposed.sql.transactions.transaction

class CommitContentRepositoryImpl : CommitContentRepository {
    override fun find(sha: String): CommitContent? {
        return transaction {
            FileContentDao.find {
                FileContents.commit eq CommitDao.find { Commits.sha eq sha }.single().id
            }.firstOrNull()?.asModel()
        }
    }

    override fun save(content: CommitContent): CommitContent {
        return transaction {
            FileContentDao.new {
                this.commit = CommitDao.find { Commits.sha eq content.commit.sha }.single()
                this.files = content.files
            }.asModel()
        }
    }
}
