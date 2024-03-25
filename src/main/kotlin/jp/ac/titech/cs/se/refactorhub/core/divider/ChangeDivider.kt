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

/**
 * divide last commit into 2 commits to create an intermediate commit
 * @param filesToBeModified files to be added or modified in order to create intermediate commit
 * @param filePathsToBeRemoved file paths to be removed in order to create intermediate commit
 * @param originalCommit the original commit the user is annotating
 * @param currentSnapshots snapshots before executing this method
 */
fun divideLastCommit(
    userName: String,
    latestInternalCommitSha: String,
    filesToBeModified: List<File>,
    filePathsToBeRemoved: List<String>,
    originalCommit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
): AppendResult {
    return Git.appendCommitBeforeLatest(userName, latestInternalCommitSha, filesToBeModified, filePathsToBeRemoved, originalCommit, currentSnapshots)
}

/**
 * modify 1 file at last intermediate commit
 * @param originalCommit the original commit the user is annotating
 * @param currentSnapshots snapshots before executing this method
 */
fun modifyLastIntermediateCommit(
    userName: String,
    latestInternalCommitSha: String,
    fileToBeModified: File,
    isRemoved: Boolean,
    originalCommit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
): ModifyResult {
    return Git.modifyCommitBeforeLatest(userName, latestInternalCommitSha, fileToBeModified, isRemoved, originalCommit, currentSnapshots)
}

/**
 * remove last intermediate commit
 * @param originalCommit the original commit the user is annotating
 * @param currentSnapshots snapshots before executing this method
 */
fun removeLastIntermediateCommit(
    userName: String,
    latestInternalCommitSha: String,
    originalCommit: CommitFileData,
    currentSnapshots: List<SnapshotFileData>
): RemoveResult {
    return Git.removeCommitBeforeLatest(userName, latestInternalCommitSha, originalCommit, currentSnapshots)
}
