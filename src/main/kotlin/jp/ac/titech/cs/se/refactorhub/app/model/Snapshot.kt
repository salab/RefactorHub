package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import java.util.UUID

data class Snapshot(
    val id: UUID,
    val files: List<File>,
    val fileMappings: List<FileMapping>,
    val patch: String,
    val changes: List<Change>
)

data class SnapshotData(
    val patch: String,
    val changes: List<jp.ac.titech.cs.se.refactorhub.core.model.change.Change>
)
