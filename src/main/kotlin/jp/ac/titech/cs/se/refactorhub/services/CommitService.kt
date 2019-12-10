package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.BadRequestException
import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.exceptions.UnprocessableEntityException
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.repositories.CommitRepository
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class CommitService(
    private val commitRepository: CommitRepository,
    private val gitHubService: GitHubService
) {

    fun get(sha: String): Commit {
        val commit = commitRepository.findBySha(sha)
        if (commit.isPresent) return commit.get()
        throw NotFoundException("Commit(sha=$sha) is not found.")
    }

    fun getInfo(sha: String): Commit.Info {
        val commit = gitHubService.getCommit(get(sha))
        return Commit.Info(
            commit.shA1,
            commit.owner.ownerName,
            commit.owner.name,
            commit.htmlUrl.toExternalForm(),
            commit.commitShortInfo.message,
            commit.commitShortInfo.author.name,
            commit.commitShortInfo.authoredDate,
            commit.files.map {
                Commit.Info.File(
                    it.sha,
                    it.status,
                    it.fileName,
                    it.previousFilename ?: it.fileName
                )
            },
            commit.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported.")
        )
    }

    fun create(sha: String, owner: String, repository: String): Commit {
        val commit = commitRepository.findBySha(sha)
        return if (commit.isPresent) commit.get()
        else commitRepository.save(Commit(sha, owner, repository))
    }

    fun createByUrl(url: String): Commit {
        val commit = tryCreate(url)
        return create(commit.sha, commit.owner, commit.repository)
    }

    fun tryCreate(url: String): Commit {
        val result = GITHUB_COMMIT_URL.matchEntire(url)
            ?: throw UnprocessableEntityException("url doesn't match GitHub commit URL.")
        val (owner, repository, sha) = result.destructured
        val commit = try {
            gitHubService.getCommit(sha, owner, repository)
        } catch (e: IOException) {
            throw UnprocessableEntityException("repository or commit doesn't exist on GitHub.", e)
        }
        return Commit(commit.shA1, owner, repository)
    }

    companion object {
        val GITHUB_COMMIT_URL = """https://github.com/([a-zA-Z0-9\-]+)/([\w\-]+)/commit/([a-fA-F0-9]+)/?""".toRegex()
    }

}
