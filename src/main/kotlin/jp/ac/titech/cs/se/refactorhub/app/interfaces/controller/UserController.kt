package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserController : KoinComponent {

    private val userService: UserService by inject()

    fun get(id: Int): User {
        return userService.get(id)
    }
}
