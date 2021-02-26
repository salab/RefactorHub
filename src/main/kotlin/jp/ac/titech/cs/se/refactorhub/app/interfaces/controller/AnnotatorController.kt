package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.usecase.service.AnnotatorService
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileContent
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class AnnotatorController : KoinComponent {
    private val annotatorService: AnnotatorService by inject()

    fun getFileContent(
        owner: String,
        repository: String,
        sha: String,
        category: DiffCategory,
        path: String
    ): FileContent {
        return annotatorService.getFileContent(owner, repository, sha, category, path)
    }

    fun prepareCommitContents(): Boolean {
        return annotatorService.prepareCommitContents()
    }
}
