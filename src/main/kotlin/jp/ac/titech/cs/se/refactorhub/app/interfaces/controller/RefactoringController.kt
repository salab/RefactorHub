package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringController : KoinComponent {
    private val refactoringService: RefactoringService by inject()

    fun fork(id: Int, userId: Int?): RefactoringDraft {
        return refactoringService.fork(id, userId)
    }

    fun edit(id: Int, userId: Int?): RefactoringDraft {
        return refactoringService.edit(id, userId)
    }
}
