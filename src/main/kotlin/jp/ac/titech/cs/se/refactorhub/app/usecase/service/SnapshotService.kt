package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.SnapshotRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Change
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
        files: List<File>,
        fileMappings: List<FileMapping>,
        patch: String,
        changes: List<Change> = listOf(changeService.createEmpty())
    ): Snapshot {
        return snapshotRepository.create(files, fileMappings, patch, changes)
    }
    fun createFromCommit(commitId: UUID): Snapshot {
        val commit = commitService.get(commitId)
        return snapshotRepository.create(
            commit.afterFiles,
            commit.fileMappings,
            commit.patch,
            listOf(changeService.createEmpty())
        )
    }

    fun update(snapshot: Snapshot): Snapshot {
        return snapshotRepository.updateById(snapshot.id, snapshot.files, snapshot.fileMappings, snapshot.patch, snapshot.changes)
    }
    fun appendChange(snapshotId: UUID): Snapshot {
        val changes = get(snapshotId).changes.toMutableList()
        changes.add(changeService.createEmpty())
        return snapshotRepository.updateById(snapshotId, changes = changes)
    }
    fun removeChange(snapshotId: UUID, changeId: UUID): Snapshot {
        val snapshot = get(snapshotId)
        if (snapshot.changes.find { it.id == changeId } == null) return snapshot
        val newChanges = snapshot.changes.filter { it.id != changeId }
        val newSnapshot = snapshotRepository.updateById(snapshotId, changes = newChanges)
        changeService.delete(changeId)
        return newSnapshot
    }

    fun delete(snapshotId: UUID) {
        val snapshot = get(snapshotId)
        snapshotRepository.updateById(snapshotId, changes = listOf())
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
