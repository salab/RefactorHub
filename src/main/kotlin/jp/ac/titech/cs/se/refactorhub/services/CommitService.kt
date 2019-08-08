package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.repositories.CommitRepository
import org.springframework.stereotype.Service

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
            }
        )
    }

    fun create(sha: String, owner: String, repository: String): Commit {
        val commit = commitRepository.findBySha(sha)
        return if (commit.isPresent) commit.get()
        else commitRepository.save(Commit(sha, owner, repository))
    }

}
