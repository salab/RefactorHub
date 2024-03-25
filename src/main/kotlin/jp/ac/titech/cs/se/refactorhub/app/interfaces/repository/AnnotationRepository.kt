package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationOverview
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import java.util.UUID

interface AnnotationRepository {
    fun findById(id: UUID): Annotation?
    fun findByOwnerId(ownerId: UUID): List<AnnotationOverview>
    fun findByExperimentId(ownerId: UUID, experimentId: UUID): List<Annotation>
    fun findByCommitId(ownerId: UUID, experimentId: UUID, commitId: UUID): Annotation?

    fun create(
        ownerId: UUID,
        commitId: UUID,
        experimentId: UUID,
        isDraft: Boolean,
        hasTemporarySnapshot: Boolean,
        latestInternalCommitSha: String,
    ): Annotation

    fun updateById(
        id: UUID,
        isDraft: Boolean? = null,
        hasTemporarySnapshot: Boolean? = null,
        latestInternalCommitSha: String? = null,
    ): Annotation

    fun deleteById(id: UUID)
}
