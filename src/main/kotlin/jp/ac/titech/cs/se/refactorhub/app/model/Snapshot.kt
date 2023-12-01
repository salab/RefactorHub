package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import jp.ac.titech.cs.se.refactorhub.core.model.divider.SnapshotFileData
import java.util.UUID

data class Snapshot(
    val id: UUID,
    override val files: List<File>,
    override val fileMappings: List<FileMapping>,
    override val patch: String,
    val changes: List<Change>
): SnapshotFileData

data class SnapshotData(
    val patch: String,
    val changes: List<jp.ac.titech.cs.se.refactorhub.core.model.change.Change>
)
