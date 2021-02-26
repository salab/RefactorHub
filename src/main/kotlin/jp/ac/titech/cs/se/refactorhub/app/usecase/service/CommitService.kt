package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFile
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFileStatus
import org.kohsuke.github.GitHub
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class CommitService : KoinComponent {
    private val commitRepository: CommitRepository by inject()

    fun get(owner: String, repository: String, sha: String): Commit {
        val commit = commitRepository.find(owner, repository, sha)
        commit ?: throw NotFoundException("Commit(owner=$owner, repository=$repository, sha=$sha) is not found")
        return commit
    }

    fun getAll(): List<Commit> {
        return commitRepository.findAll()
    }

    fun getDetail(owner: String, repository: String, sha: String): CommitDetail {
        val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)
        return client.getRepository("$owner/$repository").getCommit(sha).let {
            CommitDetail(
                it.owner.ownerName,
                it.owner.name,
                it.shA1,
                it.htmlUrl.toExternalForm(),
                it.commitShortInfo.message,
                it.commitShortInfo.author.name,
                it.commitShortInfo.authoredDate,
                it.files.map { file ->
                    CommitFile(
                        file.sha,
                        CommitFileStatus.valueOf(file.status),
                        file.fileName,
                        file.previousFilename ?: file.fileName,
                        file.patch ?: ""
                    )
                },
                it.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported")
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
