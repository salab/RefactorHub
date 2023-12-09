package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationData
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.AnnotationService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ChangeService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.SnapshotService
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class AnnotationController : KoinComponent {
    private val annotationService: AnnotationService by inject()
    private val snapshotService: SnapshotService by inject()
    private val changeService: ChangeService by inject()

    fun getAnnotation(annotationId: UUID): Annotation {
        return annotationService.get(annotationId)
    }
    fun getAnnotationData(annotationId: UUID): AnnotationData {
        return annotationService.getData(annotationId)
    }

    fun publishAnnotation(annotationId: UUID, isDraft: Boolean, userId: UUID?): Annotation.IsDraft {
        annotationService.verifyIds(userId, annotationId)
        return annotationService.publish(annotationId, isDraft)
    }

    fun appendTemporarySnapshot(annotationId: UUID, changeId: UUID, userId: UUID?): Annotation.HasTemporarySnapshotAndSnapshots {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId)
        return annotationService.appendTemporarySnapshot(annotationId, changeId, verifiedUserId)
    }
    fun modifyTemporarySnapshot(annotationId: UUID, filePath: String, fileContent: String, isRemoved: Boolean, userId: UUID?): Annotation.Snapshots {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId)
        return annotationService.modifyTemporarySnapshot(annotationId, filePath, fileContent, isRemoved, verifiedUserId)
    }
    fun settleTemporarySnapshot(annotationId: UUID, userId: UUID?): Annotation.HasTemporarySnapshot {
        annotationService.verifyIds(userId, annotationId)
        return annotationService.settleTemporarySnapshot(annotationId)
    }

    fun appendChange(annotationId: UUID, snapshotId: UUID, userId: UUID?): Snapshot {
        annotationService.verifyIds(userId, annotationId, snapshotId)
        return snapshotService.appendChange(snapshotId)
    }
    fun updateChange(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        description: String,
        typeName: String,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.update(changeId, description, typeName, verifiedUserId)
    }
    fun removeChange(annotationId: UUID, snapshotId: UUID, changeId: UUID, userId: UUID?): Annotation.HasTemporarySnapshotAndSnapshots {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return annotationService.removeChange(annotationId, snapshotId, changeId, verifiedUserId)
    }


    fun putNewParameter(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        elementType: String,
        multiple: Boolean,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.putNewParameter(changeId, diffCategory, parameterName, elementType, multiple, verifiedUserId)
    }
    fun removeParameter(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.removeParameter(changeId, diffCategory, parameterName, verifiedUserId)
    }
    fun verifyParameter(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        isVerified: Boolean,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.verifyParameter(changeId, diffCategory, parameterName, isVerified, verifiedUserId)
    }
    fun appendParameterElement(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.appendParameterElement(changeId, diffCategory, parameterName, verifiedUserId)
    }
    fun updateParameterValue(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        elementIndex: Int,
        element: CodeElement,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.updateParameterValue(changeId, diffCategory, parameterName, elementIndex, element, verifiedUserId, annotationId, snapshotId)
    }
    fun clearParameterValue(
        annotationId: UUID,
        snapshotId: UUID,
        changeId: UUID,
        diffCategory: DiffCategory,
        parameterName: String,
        elementIndex: Int,
        userId: UUID?
    ): Change {
        val verifiedUserId = annotationService.verifyIds(userId, annotationId, snapshotId, changeId)
        return changeService.clearParameterValue(changeId, diffCategory, parameterName, elementIndex, verifiedUserId)
    }
}
