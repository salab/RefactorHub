package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.SnapshotRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class SnapshotService : KoinComponent {
    private val commitService: CommitService by inject()
    private val changeService: ChangeService by inject()
    private val snapshotRepository: SnapshotRepository by inject()

    fun get(snapshotId: UUID): Snapshot {
        val snapshot = snapshotRepository.findById(snapshotId)
        return snapshot ?: throw NotFoundException("Snapshot(id=$snapshotId) is not found")
    }

    fun create(
        annotationId: UUID,
        orderIndex: Int,
        files: List<File>,
        fileMappings: List<FileMapping>,
        patch: String,
    ): Snapshot {
        val snapshotId = snapshotRepository.create(annotationId, orderIndex, files, fileMappings, patch).id
        changeService.createEmpty(snapshotId, 0)
        return get(snapshotId)
    }
    fun createFromCommit(annotationId: UUID, orderIndex: Int, commitId: UUID): Snapshot {
        val commit = commitService.get(commitId)
        val snapshotId = snapshotRepository.create(
            annotationId,
            orderIndex,
            commit.afterFiles,
            commit.fileMappings,
            commit.patch
        ).id
        changeService.createEmpty(snapshotId, 0)
        return get(snapshotId)
    }

    fun update(snapshot: Snapshot): Snapshot {
        return snapshotRepository.updateById(snapshot.id, snapshot.orderIndex, snapshot.files, snapshot.fileMappings, snapshot.patch)
    }
    fun appendChange(snapshotId: UUID): Snapshot {
        val changes = get(snapshotId).changes
        changeService.createEmpty(snapshotId, changes.size)
        return get(snapshotId)
    }
    fun removeChange(snapshotId: UUID, changeId: UUID): Snapshot {
        val oldSnapshot = get(snapshotId)
        if (oldSnapshot.changes.find { it.id == changeId } == null) return oldSnapshot
        var orderIndex = 0
        oldSnapshot.changes.forEach {
            if (it.id == changeId) return@forEach
            changeService.updateOrderIndex(it.id, orderIndex)
            orderIndex++
        }
        changeService.delete(changeId)
        return get(snapshotId)
    }

    fun delete(snapshotId: UUID) {
        val snapshot = get(snapshotId)
        snapshot.changes.forEach { changeService.delete(it.id) }
        snapshotRepository.deleteById(snapshotId)
    }

    fun isInDiffHunk(
        snapshotId: UUID,
        diffCategory: DiffCategory,
        filePath: String,
        queryLineNumber: Int
    ): Boolean {
        return get(snapshotId).fileMappings.any { fileMapping ->
            if (diffCategory == DiffCategory.before)
                fileMapping.beforePath == filePath && fileMapping.diffHunks.any {
                    it.before != null && it.before.startLine <= queryLineNumber && queryLineNumber <= it.before.endLine
                }
            else
                fileMapping.afterPath == filePath && fileMapping.diffHunks.any {
                    it.after != null && it.after.startLine <= queryLineNumber && queryLineNumber <= it.after.endLine
                }
        }
    }
}
