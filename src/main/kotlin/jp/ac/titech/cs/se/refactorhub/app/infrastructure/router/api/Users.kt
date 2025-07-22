package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.server.application.call
import io.ktor.server.locations.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.koin.ktor.ext.inject

@Location("/me")
class GetMe

@Location("/{userId}/annotations")
data class GetUserAnnotationIds(val userId: String)

fun Route.users() {
    route("/users") {
        val userController: UserController by inject()

        get<GetMe> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            call.respond(userController.getMe(userId))
        }
        get<GetUserAnnotationIds> {
            val userId = it.userId.toUUID()
            call.respond(userController.getUserAnnotationIds(userId))
        }
    }
}
