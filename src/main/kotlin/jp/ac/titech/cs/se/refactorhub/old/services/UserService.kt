package jp.ac.titech.cs.se.refactorhub.old.services

import jp.ac.titech.cs.se.refactorhub.old.exceptions.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.old.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.old.exceptions.UnauthorizedException
import jp.ac.titech.cs.se.refactorhub.old.models.Draft
import jp.ac.titech.cs.se.refactorhub.old.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.old.models.User
import jp.ac.titech.cs.se.refactorhub.old.repositories.UserRepository
import org.kohsuke.github.GHUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) : AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    override fun loadUserDetails(token: PreAuthenticatedAuthenticationToken): User? {
        val principal = token.principal
        return if (principal is GHUser) {
            if (!userRepository.existsById(principal.id)) create(principal.id, principal.login)
            else get(principal.id)
        } else null
    }

    fun get(id: Long): User {
        val user = userRepository.findById(id)
        if (user.isPresent) return user.get()
        throw NotFoundException("User(id=$id) is not found.")
    }

    fun getByName(name: String): User {
        val user = userRepository.findByName(name)
        if (user.isPresent) return user.get()
        throw NotFoundException("User(name=$name) is not found.")
    }

    fun getDrafts(id: Long): List<Draft> {
        val optional = userRepository.findByIdAndFetchDraftsEagerly(id)
        if (optional.isPresent) {
            val owner = me()
            return optional.get().also {
                if (it != owner) throw ForbiddenException("User(id=${owner.id}) is not an owner.")
            }.drafts
        }
        throw NotFoundException("User(id=$id) is not found.")
    }

    fun getRefactorings(id: Long): List<Refactoring> {
        val optional = userRepository.findByIdAndFetchRefactoringsEagerly(id)
        if (optional.isPresent) return optional.get().refactorings
        throw NotFoundException("User(id=$id) is not found.")
    }

    fun create(id: Long, name: String): User {
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
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is User) principal
        else throw UnauthorizedException("User is not logged in.")
    }
}
