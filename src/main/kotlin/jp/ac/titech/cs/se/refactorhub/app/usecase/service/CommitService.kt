package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.annotator.fetchPatchFromGitHub
import jp.ac.titech.cs.se.refactorhub.core.annotator.getFile
import jp.ac.titech.cs.se.refactorhub.core.annotator.mapFiles
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMappingStatus
import org.kohsuke.github.GitHub
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@KoinApiExtension
class CommitService : KoinComponent {
    private val commitRepository: CommitRepository by inject()

    fun get(commitId: UUID): Commit {
        val commit = commitRepository.findById(commitId)
        return commit ?: throw NotFoundException("Commit(id=$commitId) is not found")
    }

    fun createIfNotExist(owner: String, repository: String, sha: String): Commit {
        return commitRepository.find(owner, repository, sha)
            ?: fetchCommitFromGitHub(owner, repository, sha).let {
            commitRepository.create(
                it.owner,
                it.repository,
                it.sha,
                it.parentSha,
                it.url,
                it.message,
                it.authorName,
                it.authoredDateTime,
                it.beforeFiles,
                it.afterFiles,
                it.fileMappings,
                it.patch
            )
        }
    }

}

private val GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN") ?: ""

fun fetchCommitFromGitHub(owner: String, repository: String, sha: String): Commit {
    val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)
    return runCatching {
        client.getRepository("$owner/$repository").getCommit(sha)
    }.getOrElse {
        throw NotFoundException("Commit(owner=$owner, repository=$repository, sha=$sha) is not found")
    }.let { commit ->
        val parentSha = commit.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported")
        val patch = fetchPatchFromGitHub(owner, repository, sha)
        Commit(
            UUID.randomUUID(), // meaningless
            commit.owner.ownerName,
            commit.owner.name,
            commit.shA1,
            parentSha,
            commit.htmlUrl.toExternalForm(),
            commit.commitShortInfo.message,
            commit.commitShortInfo.author.name,
            LocalDateTime.ofInstant(commit.commitShortInfo.authoredDate.toInstant(), ZoneId.systemDefault()).toString(),
            commit.files
                .filter { FileMappingStatus.valueOf(it.status) != FileMappingStatus.added }
                .map { getFile(owner, repository, parentSha, it.previousFilename ?: it.fileName) },
            commit.files
                .filter { FileMappingStatus.valueOf(it.status) != FileMappingStatus.removed }
                .map { getFile(owner, repository, commit.shA1, it.fileName) },
            mapFiles(null, patch),
            patch
        )
    }
}
