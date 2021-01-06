package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFile
import org.kohsuke.github.GitHub
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class CommitService : KoinComponent {
    private val commitRepository: CommitRepository by inject()

    fun get(sha: String): Commit {
        val commit = commitRepository.findBySha(sha)
        commit ?: throw NotFoundException("Commit(sha=$sha) is not found")
        return commit
    }

    fun getDetail(sha: String): CommitDetail {
        val commit = get(sha)
        return getDetail(commit.sha, commit.owner, commit.repository)
    }

    fun getDetail(sha: String, owner: String, repository: String): CommitDetail {
        val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)
        return client.getRepository("${owner}/${repository}").getCommit(sha).let {
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
        if (commit != null) return commit
        val detail = getDetail(sha, owner, repository)
        return commitRepository.save(Commit(sha, owner, repository, detail.parent))
    }

    companion object {
        private val GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN") ?: ""
    }
}
