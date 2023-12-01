package jp.ac.titech.cs.se.refactorhub.core.divider

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.divider.CommitFileData
import jp.ac.titech.cs.se.refactorhub.core.model.divider.SnapshotFileData

/**
 * @return `latestInternalCommitSha`
 */
fun prepareDividing(userName: String, commit: CommitFileData): String {
    return Git.initializeRepository(userName, null, commit, null)
}

fun appendTemporarySnapshot(
    userName: String,
    latestInternalCommitSha: String,
    filesToBeModified: List<File>,
    filePathsToBeRemoved: List<String>,
    commit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
) {
    Git.appendCommitBeforeLatest(userName, latestInternalCommitSha, filesToBeModified, filePathsToBeRemoved, commit, currentSnapshots)
    throw NotImplementedError()
}

fun modifyTemporarySnapshot(
    userName: String,
    latestInternalCommitSha: String,
    fileToBeModified: File,
    isRemoved: Boolean,
    commit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
) {
    Git.modifyCommitBeforeLatest(userName, latestInternalCommitSha, fileToBeModified, isRemoved, commit, currentSnapshots)
    throw NotImplementedError()
}

fun removeIntermediateSnapshot(
    userName: String,
    latestInternalCommitSha: String,
    commit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
) {
    Git.removeCommitBeforeLatest(userName, latestInternalCommitSha, commit, currentSnapshots)
    throw NotImplementedError()
}
