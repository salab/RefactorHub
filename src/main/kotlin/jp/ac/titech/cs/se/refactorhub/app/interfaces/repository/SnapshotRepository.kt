package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import java.util.UUID

interface SnapshotRepository {
    fun findById(id: UUID): Snapshot?

    fun create(
        files: List<File>,
        fileMappings: List<FileMapping>,
        patch: String,
        changes: List<Change>
    ): Snapshot

    fun updateById(
        id: UUID,
        files: List<File>? = null,
        fileMappings: List<FileMapping>? = null,
        patch: String? = null,
        changes: List<Change>? = null
    ): Snapshot

    fun deleteById(id: UUID)
}
