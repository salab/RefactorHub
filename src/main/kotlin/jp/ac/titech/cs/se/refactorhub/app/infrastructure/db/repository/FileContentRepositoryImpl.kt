package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.FileContents
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.FileContentRepository
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class FileContentRepositoryImpl : FileContentRepository {
    override fun find(owner: String, repository: String, sha: String): CommitFileContents? {
        return transaction {
            FileContentDao.find {
                (FileContents.sha eq sha) and (FileContents.owner eq owner) and (FileContents.repository eq repository)
            }.singleOrNull()?.asModel()
        }
    }

    override fun save(contents: CommitFileContents): CommitFileContents {
        return transaction {
            FileContentDao.new {
                this.owner = contents.owner
                this.repository = contents.repository
                this.sha = contents.sha
                this.files = contents.files
            }.asModel()
        }
    }
}
