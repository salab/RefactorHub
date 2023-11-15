package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.SnapshotRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import jp.ac.titech.cs.se.refactorhub.app.model.ChangedFile
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
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
        files: List<ChangedFile>,
        diffHunks: List<DiffHunk>,
        patch: String,
        changes: List<Change>
    ): Snapshot {
        return transaction {
            SnapshotDao.new {
                this.files = files
                this.diffHunks = diffHunks
                this.patch = patch
            }.apply {
                this.changes = SizedCollection(changes.map { ChangeDao[it.id] })
            }.asModel()
        }
    }

    override fun updateById(
        id: UUID,
        files: List<ChangedFile>?,
        diffHunks: List<DiffHunk>?,
        patch: String?,
        changes: List<Change>?
    ): Snapshot {
        return transaction {
            val dao = SnapshotDao[id]
            if (files != null) dao.files = files
            if (diffHunks != null) dao.diffHunks = diffHunks
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
