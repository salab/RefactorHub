package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.AnnotationOverview
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
class UserController : KoinComponent {
    private val userService: UserService by inject()

    fun getUserAnnotationIds(userId: UUID): List<AnnotationOverview> {
        return userService.getUserAnnotationIds(userId)
    }

    fun getMe(userId: UUID?): User {
        return userService.getMe(userId)
    }
}
