package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.exception.UnauthorizedException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationOverview
import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class UserService : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val annotationService: AnnotationService by inject()

    fun get(userId: UUID): User {
        val user = userRepository.findById(userId)
        user ?: throw NotFoundException("User(id=$userId) is not found")
        return user
    }

    fun getUserAnnotationIds(userId: UUID): List<AnnotationOverview> {
        return annotationService.getUserAnnotations(userId)
    }

    fun getMe(userId: UUID?): User {
        userId ?: throw UnauthorizedException("You are not logged in")
        return get(userId)
    }

    fun createIfNotExist(subId: Int, name: String): User {
        val user = userRepository.findBySubId(subId)
        return user ?: userRepository.create(subId, name)
    }
}
