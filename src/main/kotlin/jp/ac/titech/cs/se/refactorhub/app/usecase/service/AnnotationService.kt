package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.AnnotationRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationData
import jp.ac.titech.cs.se.refactorhub.app.model.SnapshotData
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class AnnotationService : KoinComponent {
    private val userService: UserService by inject()
    private val experimentService: ExperimentService by inject()
    private val snapshotService: SnapshotService by inject()
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
            // TODO
            ?: throw NotImplementedError()
//            ?: annotationRepository.create(user.id, commitId, experimentId, true, false, )
    }

    fun publish(annotationId: UUID, isDraft: Boolean): Annotation.IsDraft {
        return annotationRepository.updateById(annotationId, isDraft = isDraft).pickIsDraft()
    }

    fun appendTemporarySnapshot(annotationId: UUID, changeId: UUID): Annotation.HasTemporarySnapshotAndSnapshots {
        // TODO
        throw NotImplementedError()
    }
    fun modifyTemporarySnapshot(annotationId: UUID, filePath: String, fileContent: String, isRemoved: Boolean): Annotation.Snapshots {
        // TODO
        throw NotImplementedError()
    }
    fun settleTemporarySnapshot(annotationId: UUID): Annotation.HasTemporarySnapshot {
        // TODO
        throw NotImplementedError()
    }

    fun removeChange(annotationId: UUID, snapshotId: UUID, changeId: UUID): Annotation.Snapshots {
        // TODO
        throw NotImplementedError()
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

    private fun Annotation.data() = AnnotationData(
        userService.get(this.ownerId).name,
        this.commit,
        this.snapshots.map { SnapshotData(it.patch, it.changes) }
    )
}
