package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitContentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileContent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class AnnotatorService : KoinComponent {
    private val commitContentRepository: CommitContentRepository by inject()
    private val commitService: CommitService by inject()

    fun getCommitContent(
        owner: String,
        repository: String,
        sha: String
    ): CommitContent {
        val content = commitContentRepository.find(owner, repository, sha)
        if (content != null) return content
        return createCommitContent(owner, repository, sha)
    }

    private fun createCommitContent(
        owner: String,
        repository: String,
        sha: String
    ): CommitContent {
        val detail = commitService.getDetail(owner, repository, sha)
        val commit = commitService.createIfNotExist(detail.owner, detail.repository, detail.sha)
        val content = CommitContent(
            commit,
            detail.files.map {
                CommitContent.FilePair(
                    CommitContent.File(
                        it.previousName,
                        jp.ac.titech.cs.se.refactorhub.core.annotator.getFileContent(
                            commit.owner,
                            commit.repository,
                            detail.parent,
                            it.previousName
                        )
                    ),
                    CommitContent.File(
                        it.name,
                        jp.ac.titech.cs.se.refactorhub.core.annotator.getFileContent(
                            commit.owner,
                            commit.repository,
                            detail.sha,
                            it.name
                        )
                    ),
                    it.patch,
                    it.diffHunks
                )
            }
        )
        return commitContentRepository.find(commit.owner, commit.repository, commit.sha)
            ?: commitContentRepository.save(content)
    }

    fun createCommitContentIfNotExist(
        commit: Commit
    ) {
        commitContentRepository.find(commit.owner, commit.repository, commit.sha)
            ?: createCommitContent(commit.owner, commit.repository, commit.sha)
    }

    fun getFileContent(
        owner: String,
        repository: String,
        sha: String,
        category: DiffCategory,
        path: String
    ): FileContent {
        val content = getCommitContent(owner, repository, sha)
        return content.files.map { it.get(category) }.find { it.name == path }?.content
            ?: throw NotFoundException("File(path=$path) is not found")
    }

    private var pending: Boolean = false

    fun prepareCommitContents(): Boolean {
        if (!pending) {
            pending = true
            try {
                commitService.getAll().forEach {
                    createCommitContentIfNotExist(it)
                }
            } finally {
                pending = false
                return true
            }
        }
        return false
    }
}
