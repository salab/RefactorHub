package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import java.util.UUID

interface SnapshotRepository {
    fun findById(id: UUID): Snapshot?

    fun create(
        annotationId: UUID,
        orderIndex: Int,
        files: List<File>,
        fileMappings: List<FileMapping>,
        patch: String
    ): Snapshot

    fun updateById(
        id: UUID,
        orderIndex: Int? = null,
        files: List<File>? = null,
        fileMappings: List<FileMapping>? = null,
        patch: String? = null,
    ): Snapshot

    fun deleteById(id: UUID)
}
