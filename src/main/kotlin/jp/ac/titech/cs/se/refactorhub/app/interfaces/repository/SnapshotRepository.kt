package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import java.util.UUID

interface SnapshotRepository {
    fun findById(id: UUID): Snapshot?

    fun create(
        files: List<ChangedFile>,
        diffHunks: List<DiffHunk>,
        patch: String,
        changes: List<Change>
    ): Snapshot

    fun updateById(
        id: UUID,
        files: List<ChangedFile>? = null,
        diffHunks: List<DiffHunk>? = null,
        patch: String? = null,
        changes: List<Change>? = null
    ): Snapshot

    fun deleteById(id: UUID)
}
