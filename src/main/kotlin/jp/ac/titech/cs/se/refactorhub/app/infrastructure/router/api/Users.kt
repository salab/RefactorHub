package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class GetUser(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/drafts")
data class GetUserDrafts(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/refactorings")
data class GetUserRefactorings(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/me")
class GetMe

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.users() {
    route("/users") {
        val userController: UserController by inject()

        get<GetUser> {
            call.respond(userController.get(it.id))
        }
        get<GetUserDrafts> {
            call.respond(userController.getDrafts(it.id))
        }
        get<GetUserRefactorings> {
            call.respond(userController.getRefactorings(it.id))
        }
        get<GetMe> {
            val session = call.sessions.get<Session>()
            call.respond(userController.getMe(session?.id))
        }
    }
}
