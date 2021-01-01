package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringController : KoinComponent {
    private val refactoringService: RefactoringService by inject()

    data class CreateRefactoringBody(
        val commit: Commit,
        val type: String,
        val data: Refactoring.Data,
        val description: String
    )

    fun create(body: CreateRefactoringBody, userId: Int?): Refactoring {
        return refactoringService.create(body.commit, body.type, body.data, body.description, userId)
    }

    fun getAll(): List<Refactoring> {
        return refactoringService.getAll()
    }

    fun get(id: Int): Refactoring {
        return refactoringService.get(id)
    }

    fun getChildren(id: Int): List<Refactoring> {
        return refactoringService.getChildren(id)
    }

    fun getDrafts(id: Int): List<RefactoringDraft> {
        return refactoringService.getDrafts(id)
    }

    fun fork(id: Int, userId: Int?): RefactoringDraft {
        return refactoringService.fork(id, userId)
    }

    fun edit(id: Int, userId: Int?): RefactoringDraft {
        return refactoringService.edit(id, userId)
    }
}
