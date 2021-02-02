package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContents
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.FileContentRepository
import jp.ac.titech.cs.se.refactorhub.core.model.editor.CommitFileContents
import org.jetbrains.exposed.sql.transactions.transaction

class FileContentRepositoryImpl : FileContentRepository {
    override fun find(sha: String): CommitFileContents? {
        return transaction {
            FileContentDao.find {
                FileContents.commit eq CommitDao.find { Commits.sha eq sha }.single().id
            }.firstOrNull()?.asModel()
        }
    }

    override fun save(contents: CommitFileContents): CommitFileContents {
        return transaction {
            FileContentDao.new {
                this.commit = CommitDao.find { Commits.sha eq contents.commit.sha }.single()
                this.files = contents.files
            }.asModel()
        }
    }
}
