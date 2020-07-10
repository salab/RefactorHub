package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/refactorings")
class RefactoringController : KoinComponent {

    @Location("")
    class CreateRefactoring
    data class CreateRefactoringBody(
        val type: String,
        val description: String,
        val commit: Commit,
        val data: Refactoring.Data
    )

    @Location("")
    class GetAllRefactorings

    @Location("/{id}")
    data class GetRefactoring(val id: Int)

    @Location("/{id}/children")
    data class GetRefactoringChildren(val id: Int)

    @Location("/{id}/drafts")
    data class GetRefactoringDrafts(val id: Int)

    @Location("/{id}/fork")
    data class ForkRefactoring(val id: Int)

    @Location("/{id}/edit")
    data class EditRefactoring(val id: Int)

    fun create(params: CreateRefactoring, body: CreateRefactoringBody): Refactoring {
        TODO()
    }

    fun getAll(): List<Refactoring> {
        TODO()
    }

    fun get(params: GetRefactoring): Refactoring {
        TODO()
    }

    fun getChildren(params: GetRefactoringChildren): Refactoring {
        TODO()
    }

    fun getDrafts(params: GetRefactoringDrafts): Refactoring {
        TODO()
    }

    fun fork(params: ForkRefactoring): Refactoring {
        TODO()
    }

    fun edit(params: EditRefactoring): Refactoring {
        TODO()
    }
}
