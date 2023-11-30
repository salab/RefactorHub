package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/me")
class GetMe

@KtorExperimentalLocationsAPI
@Location("/{userId}/annotations")
data class GetUserAnnotationIds(val userId: String)

@KoinApiExtension
@KtorExperimentalLocationsAPI
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
