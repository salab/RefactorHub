package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.FileContentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitFileStatus
import jp.ac.titech.cs.se.refactorhub.tool.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.FileContent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class EditorService : KoinComponent {
    private val fileContentRepository: FileContentRepository by inject()
    private val commitService: CommitService by inject()

    fun getCommitFileContents(
        sha: String,
        owner: String,
        repository: String
    ): CommitFileContents {
        val contents = fileContentRepository.find(sha)
        if (contents != null) return contents
        val commit = commitService.getDetail(sha, owner, repository)
        return fileContentRepository.save(
            CommitFileContents(
                Commit(commit.sha, commit.owner, commit.repository),
                CommitFileContents.Files(
                    commit.files.filter { it.status != CommitFileStatus.added }.map {
                        CommitFileContents.File(
                            it.previousName,
                            jp.ac.titech.cs.se.refactorhub.tool.editor.getFileContent(
                                commit.parent,
                                commit.owner,
                                commit.repository,
                                it.previousName
                            )
                        )
                    },
                    commit.files.filter { it.status != CommitFileStatus.removed }.map {
                        CommitFileContents.File(
                            it.name,
                            jp.ac.titech.cs.se.refactorhub.tool.editor.getFileContent(
                                commit.sha,
                                commit.owner,
                                commit.repository,
                                it.name
                            )
                        )
                    }
                )
            )
        )
    }

    fun getFileContent(
        sha: String,
        owner: String,
        repository: String,
        category: DiffCategory,
        path: String
    ): FileContent {
        val contents = getCommitFileContents(sha, owner, repository)
        return (if (category == DiffCategory.before) contents.files.before else contents.files.after)
            .find { it.name == path }?.content
            ?: throw NotFoundException("File(path=$path) is not found")
    }
}
