package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import org.kohsuke.github.GitHub
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class CommitService : KoinComponent {
    private val commitRepository: CommitRepository by inject()

    fun getAll(): List<Commit> {
        return commitRepository.findAll()
    }

    fun getDetail(owner: String, repository: String, sha: String): CommitDetail {
        val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)
        return runCatching {
            client.getRepository("$owner/$repository").getCommit(sha)
        }.getOrElse {
            throw NotFoundException("Commit(owner=$owner, repository=$repository, sha=$sha) is not found")
        }.let { commit ->
            CommitDetail(
                commit.owner.ownerName,
                commit.owner.name,
                commit.shA1,
                commit.htmlUrl.toExternalForm(),
                commit.commitShortInfo.message,
                commit.commitShortInfo.author.name,
                commit.commitShortInfo.authoredDate,
                commit.files.map { file ->
                    val patch = file.patch ?: ""
                    val diffHunks = getDiffHunks(patch)
                    val linesDeleted = diffHunks.fold(0) { acc, diffHunk -> acc + (diffHunk.before?.let { it.endLine - it.startLine + 1 } ?: 0)}
                    val linesAdded = diffHunks.fold(0) { acc, diffHunk -> acc + (diffHunk.after?.let { it.endLine - it.startLine + 1 } ?: 0)}
                    assert(file.linesDeleted == linesDeleted && file.linesAdded == linesAdded)
                    CommitFile(
                        file.sha,
                        CommitFileStatus.valueOf(file.status),
                        file.fileName,
                        file.previousFilename ?: file.fileName,
                        patch,
                        diffHunks
                    )
                },
                commit.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported")
            )
        }
    }

    fun createIfNotExist(owner: String, repository: String, sha: String): Commit {
        val commit = commitRepository.find(owner, repository, sha)
        if (commit != null) return commit
        return commitRepository.save(Commit(owner, repository, sha))
    }

    companion object {
        private val GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN") ?: ""
    }
}

fun getDiffHunks(patch: String): List<DiffHunk> {
    val patchRaws = patch.split("\n").map { it + "\n" }

    val diffHunks = ArrayList<DiffHunk>()
    val before = object { var currentLine = 0; var hunkStartLine = 0; var hunkEndLine = 0; var isInHunk = false }
    val after = object { var currentLine = 0; var hunkStartLine = 0; var hunkEndLine = 0; var isInHunk = false }
    fun addDiffHunk() {
        var beforeHunk: DiffHunk.Hunk? = null
        var afterHunk: DiffHunk.Hunk? = null
        if (before.isInHunk) beforeHunk = DiffHunk.Hunk(before.hunkStartLine, before.hunkEndLine)
        if (after.isInHunk) afterHunk = DiffHunk.Hunk(after.hunkStartLine, after.hunkEndLine)
        diffHunks.add(DiffHunk(beforeHunk, afterHunk))
    }
    fun updateEndLine(diffCategory: DiffCategory) {
        if (diffCategory == DiffCategory.before) before.hunkEndLine = before.currentLine
        else after.hunkEndLine = after.currentLine
    }

    val headerRegex = Regex("""^@@ -(\d+),\d+ \+(\d+),\d+ @@""")
    for (raw in patchRaws) {
        val matchedHeader = headerRegex.find(raw)
        if (matchedHeader != null) {
            // header
            if (before.isInHunk) updateEndLine(DiffCategory.before)
            if (after.isInHunk) updateEndLine(DiffCategory.after)
            if (before.isInHunk || after.isInHunk) {
                addDiffHunk()
                before.isInHunk = false
                after.isInHunk = false
            }

            assert(matchedHeader.groups.size == 3)
            // matchedHeader.groups[0] holds the entire matched string e.g. "@@ -(g1),1 +(g2),1 @@"
            before.currentLine = matchedHeader.groups[1]!!.value.toInt()
            after.currentLine = matchedHeader.groups[2]!!.value.toInt()
            // NOTE: the line of next raw will be matchedHeader.groups[1]
            if (before.currentLine != 0) before.currentLine -= 1
            if (after.currentLine != 0) after.currentLine -= 1
            continue
        }
        if (raw.startsWith("-")) {
            // deleted line
            before.currentLine++
            if (!before.isInHunk) {
                // the start of hunk
                before.hunkStartLine = before.currentLine
                before.isInHunk = true
                if (after.isInHunk) updateEndLine(DiffCategory.after)
            }
            continue
        }
        if (raw.startsWith("+")) {
            // added line
            after.currentLine++
            if (!after.isInHunk) {
                // the start of hunk
                after.hunkStartLine = after.currentLine
                after.isInHunk = true
                if (before.isInHunk) updateEndLine(DiffCategory.before)
            }
            continue
        }
        // common line
        if (before.isInHunk) updateEndLine(DiffCategory.before)
        if (after.isInHunk) updateEndLine(DiffCategory.after)
        if (before.isInHunk || after.isInHunk) {
            addDiffHunk()
            before.isInHunk = false
            after.isInHunk = false
        }
        before.currentLine++
        after.currentLine++
    }

    if (before.isInHunk) updateEndLine(DiffCategory.before)
    if (after.isInHunk) updateEndLine(DiffCategory.after)
    if (before.isInHunk || after.isInHunk) addDiffHunk()
    return diffHunks
}
