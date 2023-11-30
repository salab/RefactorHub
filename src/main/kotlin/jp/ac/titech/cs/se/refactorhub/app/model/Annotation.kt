package jp.ac.titech.cs.se.refactorhub.app.model

import java.util.UUID

data class Annotation(
    val id: UUID,
    val ownerId: UUID,
    val commit: Commit,
    val experimentId: UUID,
    val isDraft: Boolean,
    val hasTemporarySnapshot: Boolean,
    val latestInternalCommitSha: String,
    val snapshots: List<Snapshot>
) {
    data class IsDraft(val isDraft: Boolean)
    data class HasTemporarySnapshotAndSnapshots(val hasTemporarySnapshot: Boolean, val snapshots: List<Snapshot>)
    data class HasTemporarySnapshot(val hasTemporarySnapshot: Boolean)
    data class Snapshots(val snapshots: List<Snapshot>)

    fun pickIsDraft() = IsDraft(isDraft)
    fun pickHasTemporarySnapshotAndSnapshots() = HasTemporarySnapshotAndSnapshots(hasTemporarySnapshot, snapshots)
    fun pickHasTemporarySnapshot() = HasTemporarySnapshot(hasTemporarySnapshot)
    fun pickSnapshots() = Snapshots(snapshots)
}

data class AnnotationData(
    val annotatorName: String,
    val commit: jp.ac.titech.cs.se.refactorhub.core.model.Commit,
    val snapshots: List<SnapshotData>
)
