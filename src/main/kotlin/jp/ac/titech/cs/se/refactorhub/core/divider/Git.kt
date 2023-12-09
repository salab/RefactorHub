package jp.ac.titech.cs.se.refactorhub.core.divider

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMappingStatus
import jp.ac.titech.cs.se.refactorhub.core.model.divider.CommitFileData
import jp.ac.titech.cs.se.refactorhub.core.model.divider.SnapshotFileData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val logger: Logger = LoggerFactory.getLogger("jp.ac.titech.cs.se.refactorhub.core.git")

data class CommandResult(val output: String, val exitValue: Int)
fun String.runCommand(workingDir: java.io.File = java.io.File(""), vararg args: String): CommandResult {
    val process = ProcessBuilder(*split(" ").toTypedArray(), *args)
        .directory(workingDir.absoluteFile)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    val command = "$this ${args.joinToString(" ")}"
    if (!process.waitFor(60, TimeUnit.SECONDS)) {
        val output = process.inputStream.bufferedReader().readText()
        logger.warn("${workingDir.absolutePath} # $command\n$output^C (execution timed out)")
        process.destroy()
        throw RuntimeException("execution timed out: $this")
    }

    val output = process.inputStream.bufferedReader().readText()
    val exitValue = process.exitValue()
    if (exitValue == 0) logger.debug("${workingDir.absolutePath} # $command\n$output".trim())
    else logger.warn("${workingDir.absolutePath} # $command\n$output".trim())
    return CommandResult(output, exitValue)
}

fun java.io.File.createDirectoriesAndFileIfNotExist(): java.io.File {
    parentFile?.mkdirs()
    createNewFile()
    return this
}

class Git {
    companion object {
        private const val REPOSITORY_DIRECTORY_PATH = "/repos"
        private val commitMessage = object {
            val empty = "empty commit"
            val before = "snapshot before the original commit"
            val after = "snapshot after the original commit"
            val intermediate = "intermediate snapshot"
        }

        init {
            if (!java.io.File(REPOSITORY_DIRECTORY_PATH).exists()) {
                java.io.File(REPOSITORY_DIRECTORY_PATH).mkdirs()
            }
            "git config --global user.email \"refactorhub@gmail.com\"".runCommand()
            "git config --global user.name \"RefactorHub\"".runCommand()
        }

        fun appendCommitBeforeLatest(
            userName: String,
            latestInternalCommitSha: String,
            filesToBeModified: List<File>,
            filePathsToBeRemoved: List<String>,
            commit: CommitFileData,
            currentSnapshots: List<SnapshotFileData>
        ): AppendResult {
            initializeRepository(userName, latestInternalCommitSha, commit, currentSnapshots)
            resetHard(userName, getCommitShaList(userName)[1])
            val patchBeforeNew = getPatch(
                userName,
                writeFilesAndCommit(userName, filesToBeModified, filePathsToBeRemoved, commitMessage.intermediate)
            )
            val newLatestInternalCommitSha = commitSnapshotAfterOriginalCommit(userName, commit)
            val patchAfterNew = getPatch(userName, newLatestInternalCommitSha)
            return AppendResult(patchBeforeNew, patchAfterNew, newLatestInternalCommitSha)
        }

        fun modifyCommitBeforeLatest(
            userName: String,
            latestInternalCommitSha: String,
            fileToBeModified: File,
            isRemoved: Boolean,
            commit: CommitFileData,
            currentSnapshots: List<SnapshotFileData>
        ): ModifyResult {
            initializeRepository(userName, latestInternalCommitSha, commit, currentSnapshots)
            resetHard(userName, getCommitShaList(userName)[1])
            resetMixed(userName, getCommitShaList(userName)[1])
            val patchBeforeNew = getPatch(
                userName,
                writeFilesAndCommit(
                    userName,
                    listOf(fileToBeModified),
                    if (isRemoved) listOf(fileToBeModified.path) else listOf(),
                    commitMessage.intermediate
                )
            )
            val newLatestInternalCommitSha = commitSnapshotAfterOriginalCommit(userName, commit)
            val patchAfterNew = getPatch(userName, newLatestInternalCommitSha)
            return ModifyResult(patchBeforeNew, patchAfterNew, newLatestInternalCommitSha)
        }

        fun removeCommitBeforeLatest(
            userName: String,
            latestInternalCommitSha: String,
            commit: CommitFileData,
            currentSnapshots: List<SnapshotFileData>
        ): RemoveResult {
            initializeRepository(userName, latestInternalCommitSha, commit, currentSnapshots)
            resetHard(userName, getCommitShaList(userName)[2])
            val newLatestInternalCommitSha = commitSnapshotAfterOriginalCommit(userName, commit)
            val patch = getPatch(userName, newLatestInternalCommitSha)
            return RemoveResult(patch, newLatestInternalCommitSha)
        }

        /**
         * @return `latestInternalCommitSha`
         */
        fun initializeRepository(
            userName: String,
            latestInternalCommitSha: String?,
            commit: CommitFileData,
            snapshots: List<SnapshotFileData>?
        ): String {
            createRepositoryIfNotExist(userName)
            if (latestInternalCommitSha == getLatestInternalCommitSha(userName)) return latestInternalCommitSha
            resetHard(userName, getEmptyCommitSha(userName))
            writeFilesAndCommit(userName, commit.beforeFiles, listOf(), commitMessage.before)
            if (snapshots.isNullOrEmpty()) return applyPatchAndCommit(userName, commit.patch, commitMessage.after)
            snapshots.dropLast(1).forEach { applyPatchAndCommit(userName, it.patch, commitMessage.intermediate) }
            return applyPatchAndCommit(userName, snapshots.last().patch, commitMessage.after)
        }

        /**
         * create `commit.afterFiles`, remove files which were removed or renamed by the original `commit`, then commit all
         * @return `latestInternalCommitSha`
         */
        private fun commitSnapshotAfterOriginalCommit(userName: String, commit: CommitFileData): String {
            val filePathsToBeRemoved = commit.fileMappings.filter {
                it.status == FileMappingStatus.removed || it.status == FileMappingStatus.renamed
            }.map { it.beforePath!! }
            return writeFilesAndCommit(userName, commit.afterFiles, filePathsToBeRemoved, commitMessage.after)
        }

        /**
         * @return new `sha`
         */
        private fun writeFilesAndCommit(userName: String, files: List<File>, filePathsToBeRemoved: List<String>, commitMessage: String): String {
            files.forEach {
                java.io.File("${getUserRepositoryPath(userName)}/${it.path}")
                    .createDirectoriesAndFileIfNotExist()
                    .writeText(it.text)
            }
            filePathsToBeRemoved.forEach {
                java.io.File("${getUserRepositoryPath(userName)}/${it}").delete()
            }
            return commitAll(userName, commitMessage)
        }
        /**
         * @return new `sha`
         */
        private fun applyPatchAndCommit(userName: String, patch: String, commitMessage: String): String {
            val patchFile = getUserPatchFile(userName)
            patchFile.createDirectoriesAndFileIfNotExist().writeText(patch)
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            "git apply ${patchFile.absolutePath}".runCommand(repositoryDirectory)
            patchFile.delete()
            return commitAll(userName, commitMessage)
        }

        /**
         * @return new `sha`
         */
        private fun commitAll(userName: String, commitMessage: String): String {
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            "git add .".runCommand(repositoryDirectory)
            "git commit --allow-empty".runCommand(
                repositoryDirectory,
                "-m", "\"$commitMessage\"" // commit message which may have spaces must not be split by space
            )
            return getLatestInternalCommitSha(userName)
        }
        private fun resetHard(userName: String, sha: String) {
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            "git reset --hard $sha".runCommand(repositoryDirectory)
        }
        private fun resetMixed(userName: String, sha: String) {
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            "git reset --mixed $sha".runCommand(repositoryDirectory)
        }
        private fun getPatch(userName: String, sha: String): String {
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            return "git diff $sha^..$sha".runCommand(repositoryDirectory).output
        }

        private fun getLatestInternalCommitSha(userName: String): String {
            return getCommitShaList(userName).first()
        }
        private fun getEmptyCommitSha(userName: String): String {
            return getCommitShaList(userName).last()
        }

        private fun getCommitShaList(userName: String): List<String> {
            if (!existsUserRepository(userName)) return listOf()
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            return "git log --pretty=%H".runCommand(repositoryDirectory)
                .output.split("\n").dropLast(1) // drop last ""
        }

        private fun createRepositoryIfNotExist(userName: String) {
            if (existsUserRepository(userName)) return
            val repositoryDirectory = getUserRepositoryDirectory(userName)
            repositoryDirectory.mkdirs()
            "git init".runCommand(repositoryDirectory)
            "git commit --allow-empty".runCommand(repositoryDirectory, "-m", "\"${commitMessage.empty}\"")
        }
        private fun existsUserRepository(userName: String) = java.io.File("${getUserRepositoryPath(userName)}/.git").exists()
        private fun getUserRepositoryDirectory(userName: String) = java.io.File(getUserRepositoryPath(userName))
        private fun getUserRepositoryPath(userName: String) = "$REPOSITORY_DIRECTORY_PATH/$userName"

        private fun getUserPatchFile(userName: String) = java.io.File("/diff_${userName}.patch")
    }
}

data class AppendResult(
    val patchBeforeNew: String,
    val patchAfterNew: String,
    val latestInternalCommitSha: String
)
data class ModifyResult(
    val patchBeforeNew: String,
    val patchAfterNew: String,
    val latestInternalCommitSha: String
)
data class RemoveResult(
    val patch: String,
    val latestInternalCommitSha: String
)
