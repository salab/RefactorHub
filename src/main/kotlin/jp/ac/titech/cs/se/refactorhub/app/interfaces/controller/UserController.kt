package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class UserController : KoinComponent {
    private val userService: UserService by inject()

    fun get(id: Int): User {
        return userService.get(id)
    }

    fun getDrafts(id: Int): List<RefactoringDraft> {
        return userService.getDrafts(id)
    }

    fun getRefactorings(id: Int): List<Refactoring> {
        return userService.getRefactorings(id)
    }

    fun getMe(userId: Int?): User {
        return userService.getMe(userId)
    }
}
