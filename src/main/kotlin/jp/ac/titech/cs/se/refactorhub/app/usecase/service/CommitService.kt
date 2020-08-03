package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFile
import org.kohsuke.github.GitHub
import org.koin.core.KoinComponent
import org.koin.core.inject

class CommitService : KoinComponent {
    private val commitRepository: CommitRepository by inject()

    fun get(sha: String): Commit {
        val commit = commitRepository.findBySha(sha)
        commit ?: throw NotFoundException("Commit(sha=$sha) is not found")
        return commit
    }

    fun getDetail(sha: String, token: String?): CommitDetail {
        val commit = get(sha)
        val client = if (token != null) GitHub.connectUsingOAuth(token) else GitHub.connect()
        return client.getRepository("${commit.owner}/${commit.repository}").getCommit(commit.sha).let {
            CommitDetail(
                it.shA1,
                it.owner.ownerName,
                it.owner.name,
                it.htmlUrl.toExternalForm(),
                it.commitShortInfo.message,
                it.commitShortInfo.author.name,
                it.commitShortInfo.authoredDate,
                it.files.map { file ->
                    CommitFile(
                        file.sha,
                        file.status,
                        file.fileName,
                        file.previousFilename ?: file.fileName
                    )
                },
                it.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported")
            )
        }
    }

    fun createIfNotExist(sha: String, owner: String, repository: String): Commit {
        val commit = commitRepository.findBySha(sha)
        return commit ?: commitRepository.save(Commit(sha, owner, repository))
    }
}
