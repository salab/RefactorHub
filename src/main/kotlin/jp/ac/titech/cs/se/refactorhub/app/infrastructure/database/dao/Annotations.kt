package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Annotations : UUIDTable("annotations") {
    val ownerId = reference("owner_id", Users)
    val commitId  = reference("commit_id", Commits)
    val experimentId = reference("experiment_id", Experiments)
    val isDraft = bool("is_draft").default(true)
    val hasTemporarySnapshot = bool("has_temporary_snapshot").default(false)
    val latestInternalCommitSha = varchar("latest_internal_commit_sha", 40)
}

object AnnotationToSnapshots : Table("annotation_to_snapshots") {
    val annotation = reference("annotation", Annotations)
    val snapshot = reference("snapshot", Snapshots)
    override val primaryKey = PrimaryKey(annotation, snapshot)
}

class AnnotationDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Annotation> {
    companion object : UUIDEntityClass<AnnotationDao>(Annotations)

    var owner by UserDao referencedOn Annotations.ownerId
    var commit by CommitDao referencedOn Annotations.commitId
    var experiment by ExperimentDao referencedOn Annotations.experimentId
    var isDraft by Annotations.isDraft
    var hasTemporarySnapshot by Annotations.hasTemporarySnapshot
    var latestInternalCommitSha by Annotations.latestInternalCommitSha
    var snapshots by SnapshotDao via AnnotationToSnapshots

    override fun asModel(): Annotation {
        return Annotation(
            this.id.value,
            this.owner.asModel().id,
            this.commit.asModel(),
            this.experiment.asModel().id,
            this.isDraft,
            this.hasTemporarySnapshot,
            this.latestInternalCommitSha,
            this.snapshots.map { it.asModel() }
        )
    }
}
