package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitContentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFileStatus
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
        val commit = commitService.getDetail(owner, repository, sha)
        return commitContentRepository.save(
            CommitContent(
                Commit(commit.owner, commit.repository, commit.sha),
                CommitContent.Files(
                    commit.files.filter { it.status != CommitFileStatus.added }.map {
                        CommitContent.File(
                            it.previousName,
                            jp.ac.titech.cs.se.refactorhub.core.annotator.getFileContent(
                                commit.owner,
                                commit.repository,
                                commit.parent,
                                it.previousName
                            ),
                            it.patch
                        )
                    },
                    commit.files.filter { it.status != CommitFileStatus.removed }.map {
                        CommitContent.File(
                            it.name,
                            jp.ac.titech.cs.se.refactorhub.core.annotator.getFileContent(
                                commit.owner,
                                commit.repository,
                                commit.sha,
                                it.name
                            ),
                            it.patch
                        )
                    }
                )
            )
        )
    }

    fun getFileContent(
        owner: String,
        repository: String,
        sha: String,
        category: DiffCategory,
        path: String
    ): FileContent {
        val content = getCommitContent(owner, repository, sha)
        return (if (category == DiffCategory.before) content.files.before else content.files.after)
            .find { it.name == path }?.content
            ?: throw NotFoundException("File(path=$path) is not found")
    }
}
