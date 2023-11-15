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
)
