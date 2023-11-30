package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.SnapshotRepository
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SnapshotRepositoryImpl : SnapshotRepository {
    override fun findById(id: UUID): Snapshot? {
        return transaction {
            SnapshotDao.findById(id)?.asModel()
        }
    }

    override fun create(
        files: List<File>,
        fileMappings: List<FileMapping>,
        patch: String,
        changes: List<Change>
    ): Snapshot {
        return transaction {
            SnapshotDao.new {
                this.files = files
                this.fileMappings = fileMappings
                this.patch = patch
            }.apply {
                this.changes = SizedCollection(changes.map { ChangeDao[it.id] })
            }.asModel()
        }
    }

    override fun updateById(
        id: UUID,
        files: List<File>?,
        fileMappings: List<FileMapping>?,
        patch: String?,
        changes: List<Change>?
    ): Snapshot {
        return transaction {
            val dao = SnapshotDao[id]
            if (files != null) dao.files = files
            if (fileMappings != null) dao.fileMappings = fileMappings
            if (patch != null) dao.patch = patch
            if (changes != null) dao.changes = SizedCollection(changes.map { ChangeDao[it.id] })
            dao.asModel()
        }
    }

    override fun deleteById(id: UUID) {
        transaction {
            SnapshotDao.findById(id)?.delete()
        }
    }
}
