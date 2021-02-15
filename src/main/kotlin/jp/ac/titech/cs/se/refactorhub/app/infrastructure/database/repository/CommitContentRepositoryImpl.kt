package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.FileContentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.FileContents
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitContentRepository
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class CommitContentRepositoryImpl : CommitContentRepository {
    override fun find(owner: String, repository: String, sha: String): CommitContent? {
        return transaction {
            FileContentDao.find {
                FileContents.commit eq CommitDao.find {
                    (Commits.owner eq owner) and
                        (Commits.repository eq repository) and
                        (Commits.sha eq sha)
                }.first().id
            }.firstOrNull()?.asModel()
        }
    }

    override fun save(content: CommitContent): CommitContent {
        return transaction {
            FileContentDao.new {
                this.commit = CommitDao.find {
                    (Commits.owner eq content.commit.owner) and
                        (Commits.repository eq content.commit.repository) and
                        (Commits.sha eq content.commit.sha)
                }.first()
                this.files = content.files
            }.asModel()
        }
    }
}
