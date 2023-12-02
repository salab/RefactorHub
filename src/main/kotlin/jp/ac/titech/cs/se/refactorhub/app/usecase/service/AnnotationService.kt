package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.ConflictException
import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.AnnotationRepository
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.core.annotator.createFile
import jp.ac.titech.cs.se.refactorhub.core.annotator.mapFiles
import jp.ac.titech.cs.se.refactorhub.core.divider.divideLastCommit
import jp.ac.titech.cs.se.refactorhub.core.divider.modifyLastIntermediateCommit
import jp.ac.titech.cs.se.refactorhub.core.divider.prepareDividing
import jp.ac.titech.cs.se.refactorhub.core.divider.removeLastIntermediateCommit
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMappingStatus
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class AnnotationService : KoinComponent {
    private val userService: UserService by inject()
    private val experimentService: ExperimentService by inject()
    private val commitService: CommitService by inject()
    private val snapshotService: SnapshotService by inject()
    private val changeService: ChangeService by inject()
    private val annotationRepository: AnnotationRepository by inject()

    fun get(annotationId: UUID): Annotation {
        val annotation = annotationRepository.findById(annotationId)
        return annotation ?: throw NotFoundException("Annotation(id=$annotationId) is not found")
    }
    fun getUserAnnotations(userId: UUID): List<Annotation> {
        return annotationRepository.findByOwnerId(userId)
    }

    fun getData(annotationId: UUID): AnnotationData {
        return get(annotationId).data()
    }
    fun getExperimentAnnotationsData(userId: UUID?, experimentId: UUID): List<AnnotationData> {
        val user = userService.getMe(userId)
        val experiment = experimentService.get(experimentId)
        return annotationRepository.findByExperimentId(user.id, experiment.id).map { it.data() }
    }

    fun verifyIds(userId: UUID?, annotationId: UUID, snapshotId: UUID? = null, changeId: UUID? = null): UUID {
        val user = userService.getMe(userId)
        val annotation = get(annotationId)
        if (annotation.ownerId != user.id) throw ForbiddenException("You are not Annotation(id=${annotationId})'s owner")
        if (snapshotId == null) return user.id
        val snapshot = annotation.snapshots.find { it.id == snapshotId }
            ?: throw NotFoundException("Snapshot(id=$snapshotId) is not found")
        if (changeId == null) return user.id
        if (snapshot.changes.find { it.id == changeId } == null) throw NotFoundException("Change(id=$changeId) is not found")
        return user.id
    }

    fun createAnnotationIfNotExist(userId: UUID?, experimentId: UUID, commitId: UUID): Annotation {
        val user = userService.getMe(userId)
        return annotationRepository.findByCommitId(user.id, experimentId, commitId)
            ?: annotationRepository.create(
                user.id,
                commitId,
                experimentId,
                isDraft = true,
                hasTemporarySnapshot = false,
                prepareDividing(user.name, commitService.get(commitId)),
                listOf(snapshotService.createFromCommit(commitId))
            )
    }

    fun publish(annotationId: UUID, isDraft: Boolean): Annotation.IsDraft {
        return annotationRepository.updateById(annotationId, isDraft = isDraft).pickIsDraft()
    }

    fun appendTemporarySnapshot(annotationId: UUID, changeId: UUID, userId: UUID): Annotation.HasTemporarySnapshotAndSnapshots {
        val user = userService.getMe(userId)
        val annotation = get(annotationId)
        if (annotation.hasTemporarySnapshot) throw ConflictException("Annotation(id=$annotationId) already has a temporary snapshot")
        val lastSnapshot = annotation.snapshots.last()
        val change = lastSnapshot.changes.find { it.id == changeId }
            ?: throw BadRequestException("Change(id=$changeId) is not belonging to the trailing snapshot")

        val fileMappingsToBeApplied = lastSnapshot.fileMappings.flatMap { fileMapping ->
            val diffHunksToBeApplied = fileMapping.diffHunks.filter {
                (fileMapping.beforePath != null && it.before != null
                        && change.parameterData.before.hasIntersection(fileMapping.beforePath, it.before))
                || (fileMapping.afterPath != null && it.after != null
                        && change.parameterData.after.hasIntersection(fileMapping.afterPath, it.after))
            }
            if (diffHunksToBeApplied.isEmpty())
                listOf()
            else
                listOf(FileMapping(fileMapping.status, fileMapping.beforePath, fileMapping.afterPath, diffHunksToBeApplied))
        }
        val baseFiles = if (annotation.snapshots.size == 1)
            annotation.commit.beforeFiles else annotation.snapshots[annotation.snapshots.size - 2].files
        val (newFiles, filePathsToBeRemoved) = createNewFilesAndFilePathsToBeRemoved(baseFiles, lastSnapshot, fileMappingsToBeApplied)
        val (patchBeforeNew, patchAfterNew, latestInternalCommitSha) = divideLastCommit(
            user.name,
            annotation.latestInternalCommitSha,
            newFiles,
            filePathsToBeRemoved,
            annotation.commit,
            annotation.snapshots
        )
        val fileMappingsBeforeNew = mapFiles(baseFiles, patchBeforeNew)
        val fileMappingsAfterNew = mapFiles(newFiles, patchAfterNew)

        val notAppliedFileMappings = lastSnapshot.fileMappings.flatMap { fileMapping ->
            val notAppliedDiffHunks = fileMapping.diffHunks.filter {
                !(
                        (fileMapping.beforePath != null && it.before != null
                                && change.parameterData.before.hasIntersection(fileMapping.beforePath, it.before))
                        || (fileMapping.afterPath != null && it.after != null
                                && change.parameterData.after.hasIntersection(fileMapping.afterPath, it.after))
                )
            }
            if (notAppliedDiffHunks.isEmpty())
                listOf()
            else
                listOf(FileMapping(fileMapping.status, fileMapping.beforePath, fileMapping.afterPath, notAppliedDiffHunks))
        }
        val changesBeforeNew = lastSnapshot.changes.map {
            changeService.modifyLineNumbers(it.id, notAppliedFileMappings)
        }

        val temporarySnapshot = snapshotService.update(Snapshot(
            lastSnapshot.id,
            newFiles,
            fileMappingsBeforeNew,
            patchBeforeNew,
            changesBeforeNew
        ))
        val newLastSnapshot = snapshotService.create(lastSnapshot.files, fileMappingsAfterNew, patchAfterNew)
        val newSnapshots = annotation.snapshots.apply {
            dropLast(1).toMutableList().addAll(listOf(temporarySnapshot, newLastSnapshot))
        }

        return annotationRepository.updateById(
            annotationId,
            hasTemporarySnapshot = true,
            latestInternalCommitSha = latestInternalCommitSha,
            snapshots = newSnapshots
        ).pickHasTemporarySnapshotAndSnapshots()
    }
    fun modifyTemporarySnapshot(annotationId: UUID, filePath: String, fileText: String, isRemoved: Boolean, userId: UUID): Annotation.Snapshots {
        val user = userService.getMe(userId)
        val annotation = get(annotationId)
        if (!annotation.hasTemporarySnapshot) throw ConflictException("Annotation(id=$annotationId) does not have a temporary snapshot")
        val temporarySnapshot = annotation.snapshots[annotation.snapshots.size - 2]
        val lastSnapshot = annotation.snapshots.last()

        val newFile = createFile(filePath, fileText)
        val newFiles = temporarySnapshot.files.toMutableList().apply {
            val fileToBeModified = this.find { it.path == filePath }
            if (fileToBeModified == null) {
                if (!isRemoved) add(newFile)
                return@apply
            }
            remove(fileToBeModified)
            if (isRemoved) return@apply
            add(newFile)
        }
        val baseFiles = if (annotation.snapshots.size == 2)
            annotation.commit.beforeFiles else annotation.snapshots[annotation.snapshots.size - 3].files

        val (patchBeforeNew, patchAfterNew, latestInternalCommitSha) = modifyLastIntermediateCommit(
            user.name,
            annotation.latestInternalCommitSha,
            newFile,
            isRemoved,
            annotation.commit,
            annotation.snapshots
        )
        val fileMappingsBeforeNew = mapFiles(baseFiles, patchBeforeNew)
        val fileMappingsAfterNew = mapFiles(newFiles, patchAfterNew)

        val newTemporarySnapshot = snapshotService.update(Snapshot(
            temporarySnapshot.id,
            newFiles,
            fileMappingsBeforeNew,
            patchBeforeNew,
            temporarySnapshot.changes
        ))
        val newLastSnapshot = snapshotService.update(Snapshot(
            lastSnapshot.id,
            lastSnapshot.files,
            fileMappingsAfterNew,
            patchAfterNew,
            lastSnapshot.changes
        ))
        val newSnapshots = annotation.snapshots.apply {
            dropLast(2).toMutableList().addAll(listOf(newTemporarySnapshot, newLastSnapshot))
        }

        return annotationRepository.updateById(
            annotationId,
            latestInternalCommitSha = latestInternalCommitSha,
            snapshots = newSnapshots
        ).pickSnapshots()
    }
    fun settleTemporarySnapshot(annotationId: UUID): Annotation.HasTemporarySnapshot {
        val annotation = get(annotationId)
        if (!annotation.hasTemporarySnapshot) throw ConflictException("Annotation(id=$annotationId) does not have a temporary snapshot")
        return annotationRepository.updateById(annotationId, hasTemporarySnapshot = false).pickHasTemporarySnapshot()
    }

    fun removeChange(annotationId: UUID, snapshotId: UUID, changeId: UUID, userId: UUID): Annotation.Snapshots {
        val user = userService.getMe(userId)
        val annotation = get(annotationId)
        val snapshotIndex = annotation.snapshots.indexOfFirst { it.id == snapshotId }
        val isLastIntermediateSnapshot = snapshotIndex == annotation.snapshots.size - 2
        val snapshot = snapshotService.removeChange(snapshotId, changeId)
        if (!isLastIntermediateSnapshot || snapshot.changes.isNotEmpty() || annotation.snapshots.size == 1) {
            val newSnapshots = annotation.snapshots.toMutableList()
            newSnapshots[snapshotIndex] = snapshot
            return annotationRepository.updateById(annotationId, snapshots = newSnapshots).pickSnapshots()
        }

        val lastSnapshot = annotation.snapshots.last()
        val baseFiles = if (annotation.snapshots.size == 2)
            annotation.commit.beforeFiles else annotation.snapshots[annotation.snapshots.size - 3].files
        val (patch, latestInternalCommitSha) = removeLastIntermediateCommit(
            user.name,
            annotation.latestInternalCommitSha,
            annotation.commit,
            annotation.snapshots
        )
        snapshotService.delete(snapshotId)
        val fileMapping = mapFiles(baseFiles, patch)
        val newLastSnapshot = snapshotService.update(
            Snapshot(
            lastSnapshot.id,
            lastSnapshot.files,
            fileMapping,
            patch,
            listOf(changeService.createEmpty())
        ))
        val newSnapshots = annotation.snapshots.dropLast(2).toMutableList().apply { add(newLastSnapshot) }
        return annotationRepository.updateById(
            annotationId,
            hasTemporarySnapshot = false,
            latestInternalCommitSha = latestInternalCommitSha,
            snapshots = newSnapshots
        ).pickSnapshots()
    }

    fun getFileCodeElementsMap(
        annotationId: UUID,
        snapshotId: UUID,
        diffCategory: DiffCategory
    ): Map<String, List<CodeElement>> {
        val annotation = get(annotationId)
        val snapshot = snapshotService.get(snapshotId)
        var snapshotIndex = annotation.snapshots.indexOf(snapshot)
        if (snapshotIndex == -1) throw NotFoundException("Snapshot(id=$snapshotId) is not found")
        if (diffCategory == DiffCategory.before) snapshotIndex--
        val files = if (snapshotIndex == -1) annotation.commit.beforeFiles else annotation.snapshots[snapshotIndex].files
        return files.associate { it.path to it.elements }
    }

    private fun createNewFilesAndFilePathsToBeRemoved(baseFiles: List<File>, lastSnapshot: Snapshot, fileMappingsToBeApplied: List<FileMapping>): Pair<List<File>, List<String>> {
        val baseFileTextMap = baseFiles.associate { it.path to it.text }
        val lastFileTextMap = lastSnapshot.files.associate { it.path to it.text }

        val newFileTextMap = baseFileTextMap.toMutableMap() // files in temporary snapshot
        val filePathsToBeRemoved = mutableListOf<String>()
        // added
        fileMappingsToBeApplied.filter { it.status == FileMappingStatus.added }.forEach {
            newFileTextMap[it.afterPath!!] = lastFileTextMap[it.afterPath]!!
        }
        // removed
        fileMappingsToBeApplied.filter { it.status == FileMappingStatus.removed }.forEach {
            newFileTextMap.remove(it.beforePath!!)
            filePathsToBeRemoved.add(it.beforePath)
        }
        // modified
        fun applyDiffHunks(baseFileText: String, lastFileText: String, diffHunksToBeApplied: List<DiffHunk>): String {
            val newText = StringBuilder()
            val baseFileRawList = baseFileText.split("\n")
            val lastFileRawList = lastFileText.split("\n")
            fun isRemoved(baseLineNumber: Int) = diffHunksToBeApplied.any {
                it.before != null && it.before.startLine <= baseLineNumber && baseLineNumber <= it.before.endLine
            }
            fun getAddedHunk(baseLineNumber: Int) = diffHunksToBeApplied.find {
                it.after != null && it.after.oppositeLine == baseLineNumber
            }?.after

            // when baseLineNumber = 0
            getAddedHunk(0)?.let {
                for (lastLineNumber in it.startLine..it.endLine) {
                    newText.append(lastFileRawList[lastLineNumber - 1])
                    if (lastLineNumber - 1 != lastFileRawList.size - 1) newText.append("\n")
                }
            }
            for (i in baseFileRawList.indices) {
                val baseLineNumber = i + 1
                if (!isRemoved(baseLineNumber)) {
                    newText.append(baseFileRawList[i])
                    if (i != baseFileRawList.size - 1) newText.append("\n")
                }
                getAddedHunk(baseLineNumber)?.let {
                    for (lastLineNumber in it.startLine..it.endLine) {
                        newText.append(lastFileRawList[lastLineNumber - 1])
                        if (lastLineNumber - 1 != lastFileRawList.size - 1) newText.append("\n")
                    }
                }
            }

            return newText.toString()
        }
        fileMappingsToBeApplied.filter { it.status == FileMappingStatus.modified }.forEach {
            newFileTextMap[it.afterPath!!] = applyDiffHunks(
                baseFileTextMap[it.beforePath]!!,
                lastFileTextMap[it.afterPath]!!,
                it.diffHunks
            )
        }
        // renamed
        fileMappingsToBeApplied.filter { it.status == FileMappingStatus.renamed }.forEach {
            newFileTextMap.remove(it.beforePath!!)
            filePathsToBeRemoved.add(it.beforePath)
            newFileTextMap[it.afterPath!!] = applyDiffHunks(
                baseFileTextMap[it.beforePath]!!,
                lastFileTextMap[it.afterPath]!!,
                it.diffHunks
            )
        }

        val newFiles = newFileTextMap.map { (path, text) -> createFile(path, text) }
        return Pair(newFiles, filePathsToBeRemoved)
    }

    private fun Annotation.data() = AnnotationData(
        userService.get(this.ownerId).name,
        this.commit,
        this.snapshots.map { SnapshotData(it.patch, it.changes) }
    )
}

private fun MutableMap<String, CodeElementHolder>.hasIntersection(path: String, hunk: DiffHunk.Hunk): Boolean {
    return this.any { (_, codeElementHolder) ->
        codeElementHolder.elements.any {
            val location = it.location ?: return false
            val range = location.range ?: return false
            location.path == path && range.startLine >= hunk.endLine && range.endLine <= hunk.startLine
        }
    }
}
