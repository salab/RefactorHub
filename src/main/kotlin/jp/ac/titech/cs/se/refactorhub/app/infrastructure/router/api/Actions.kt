package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ActionController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class PostAction

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.actions() {
    route("/actions") {
        val actionController: ActionController by inject()

        post<PostAction> {
            val session = call.sessions.get<Session>()
            val body = call.receive<ActionController.ActionBody>()
            call.respond(actionController.log(body.name, body.type, body.data, session?.id))
        }
    }
}
