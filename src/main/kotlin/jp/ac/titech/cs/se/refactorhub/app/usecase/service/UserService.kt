package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import io.ktor.features.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserService : KoinComponent {

    private val userRepository: UserRepository by inject()

    fun get(id: Int): User {
        val user = userRepository.findById(id)
        user ?: throw NotFoundException("User(id=$id) is not found")
        return user
    }

    fun createIfNotExist(subId: Int, name: String): User {
        val user = userRepository.findBySubId(subId)
        return user ?: userRepository.create(subId, name)
    }
}
