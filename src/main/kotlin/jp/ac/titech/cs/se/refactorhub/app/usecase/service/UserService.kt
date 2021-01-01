package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.exception.UnauthorizedException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class UserService : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val refactoringService: RefactoringService by inject()
    private val refactoringDraftService: RefactoringDraftService by inject()

    fun get(id: Int): User {
        val user = userRepository.findById(id)
        user ?: throw NotFoundException("User(id=$id) is not found")
        return user
    }

    fun getDrafts(id: Int): List<RefactoringDraft> {
        return refactoringDraftService.getUserDrafts(id)
    }

    fun getRefactorings(id: Int): List<Refactoring> {
        return refactoringService.getUserRefactorings(id)
    }

    fun getMe(id: Int?): User {
        id ?: throw UnauthorizedException("You are not logged in")
        return get(id)
    }

    fun createIfNotExist(subId: Int, name: String): User {
        val user = userRepository.findBySubId(subId)
        return user ?: userRepository.create(subId, name)
    }
}
