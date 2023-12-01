package jp.ac.titech.cs.se.refactorhub.core.model.divider

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping

interface SnapshotFileData {
    val files: List<File>
    val fileMappings: List<FileMapping>
    val patch: String
}
