package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import java.util.UUID

data class Snapshot(
    val id: UUID,
    val files: List<ChangedFile>,
    val diffHunks: List<DiffHunk>,
    val patch: String,
    val changes: List<Change>
)
