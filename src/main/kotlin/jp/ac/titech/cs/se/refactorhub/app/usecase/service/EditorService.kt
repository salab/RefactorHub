package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.FileContentRepository
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.FileContent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class EditorService : KoinComponent {
    private val fileContentRepository: FileContentRepository by inject()
    private val commitService: CommitService by inject()

    fun getFileContent(
        sha: String,
        owner: String,
        repository: String,
        path: String
    ): FileContent {
        val contents = fileContentRepository.find(owner, repository, sha) ?: run {
            val commit = commitService.getDetail(sha, owner, repository)
            fileContentRepository.save(
                CommitFileContents(
                    commit.owner,
                    commit.repository,
                    commit.sha,
                    commit.files.map {
                        CommitFileContents.File(
                            it.name,
                            jp.ac.titech.cs.se.refactorhub.tool.editor.getFileContent(sha, owner, repository, it.name)
                        )
                    }
                )
            )
        }
        return contents.files.find { it.name == path }?.content
            ?: throw NotFoundException("File(path=$path) is not found")
    }
}
