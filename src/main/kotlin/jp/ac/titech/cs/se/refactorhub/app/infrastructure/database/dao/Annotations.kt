package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationOverview
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Annotations : UUIDTable("annotations") {
    val ownerId = reference("owner_id", Users)
    val commitId  = reference("commit_id", Commits)
    val experimentId = reference("experiment_id", Experiments)
    val isDraft = bool("is_draft").default(true)
    val hasTemporarySnapshot = bool("has_temporary_snapshot").default(false)
    val latestInternalCommitSha = varchar("latest_internal_commit_sha", 40)
}

class AnnotationDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Annotation> {
    companion object : UUIDEntityClass<AnnotationDao>(Annotations)

    var owner by UserDao referencedOn Annotations.ownerId
    var commit by CommitDao referencedOn Annotations.commitId
    var experiment by ExperimentDao referencedOn Annotations.experimentId
    var isDraft by Annotations.isDraft
    var hasTemporarySnapshot by Annotations.hasTemporarySnapshot
    var latestInternalCommitSha by Annotations.latestInternalCommitSha
    val snapshots by SnapshotDao referrersOn Snapshots.annotationId

    override fun asModel(): Annotation {
        return Annotation(
            this.id.value,
            this.owner.id.value,
            this.commit.asModel(),
            this.experiment.id.value,
            this.isDraft,
            this.hasTemporarySnapshot,
            this.latestInternalCommitSha,
            this.snapshots.sortedBy { it.orderIndex }.map { it.asModel() }
        )
    }
}

class AnnotationOverviewDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<AnnotationOverview> {
    companion object : UUIDEntityClass<AnnotationOverviewDao>(Annotations)

    var owner by UserDao referencedOn Annotations.ownerId
    var commit by CommitDao referencedOn Annotations.commitId
    var experiment by ExperimentDao referencedOn Annotations.experimentId
    var isDraft by Annotations.isDraft

    override fun asModel(): AnnotationOverview {
        return AnnotationOverview(
            this.experiment.id.value,
            this.commit.id.value,
            this.id.value,
            this.isDraft,
        )
    }
}
