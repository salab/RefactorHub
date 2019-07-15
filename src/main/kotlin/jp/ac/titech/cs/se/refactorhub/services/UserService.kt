package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.exceptions.UnauthorizedException
import jp.ac.titech.cs.se.refactorhub.models.User
import jp.ac.titech.cs.se.refactorhub.repositories.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun get(id: Int): User {
        val user = userRepository.findById(id)
        if (user.isPresent) return user.get()
        throw NotFoundException("User(id=$id) is not found.")
    }

    fun getByName(name: String): User {
        val user = userRepository.findByName(name)
        if (user.isPresent) return user.get()
        throw NotFoundException("User(name=$name) is not found.")
    }

    fun create(id: Int, name: String): User {
        val user = userRepository.findById(id)
        return if (user.isPresent) {
            val u = user.get()
            u.name = name
            userRepository.save(u)
        } else {
            userRepository.save(User(id, name))
        }
    }

    fun me(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return if (principal is User.Principal) get(principal.id)
        else throw UnauthorizedException("User is not logged in.")
    }

}
