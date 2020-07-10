package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
import org.koin.core.KoinComponent
import org.koin.core.inject

@KtorExperimentalLocationsAPI
@Location("/users")
class UserController : KoinComponent {
    private val userService: UserService by inject()

    @Location("/{id}")
    data class GetUser(val id: Int)

    @Location("/{id}/drafts")
    data class GetUserDrafts(val id: Int)

    @Location("/{id}/refactorings")
    data class GetUserRefactorings(val id: Int)

    @Location("/me")
    class GetMe

    fun get(params: GetUser): User {
        return userService.get(params.id)
    }

    fun getDrafts(params: GetUserDrafts): List<RefactoringDraft> {
        TODO()
    }

    fun getRefactorings(params: GetUserRefactorings): List<Refactoring> {
        TODO()
    }

    fun getMe(): User {
        TODO()
    }
}
