package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.SnapshotRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class SnapshotService : KoinComponent {
    private val changeService: ChangeService by inject()
    private val snapshotRepository: SnapshotRepository by inject()

    fun get(snapshotId: UUID): Snapshot {
        val snapshot = snapshotRepository.findById(snapshotId)
        return snapshot ?: throw NotFoundException("Snapshot(id=$snapshotId) is not found")
    }

    fun appendChange(snapshotId: UUID): Snapshot {
        val changes = get(snapshotId).changes.toMutableList()
        changes.add(changeService.create())
        return snapshotRepository.updateById(snapshotId, changes = changes)
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
