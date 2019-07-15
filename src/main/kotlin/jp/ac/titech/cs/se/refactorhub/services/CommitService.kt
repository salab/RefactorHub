package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.repositories.CommitRepository
import org.springframework.stereotype.Service

@Service
class CommitService(
    private val commitRepository: CommitRepository
) {

    fun get(sha: String): Commit {
        val commit = commitRepository.findBySha(sha)
        if (commit.isPresent) return commit.get()
        throw NotFoundException("Commit(sha=$sha) is not found.")
    }

    fun create(sha: String, owner: String, repository: String): Commit {
        val commit = commitRepository.findBySha(sha)
        return if (commit.isPresent) commit.get()
        else commitRepository.save(Commit(sha, owner, repository))
    }

}
