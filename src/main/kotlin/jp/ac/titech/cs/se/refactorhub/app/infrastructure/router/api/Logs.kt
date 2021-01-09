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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.LogController
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class PostLog

@KtorExperimentalLocationsAPI
fun Route.logs() {
    route("/logs") {
        val logController: LogController by inject()

        post<PostLog> {
            val session = call.sessions.get<Session>()
            val body = call.receive<LogController.LogBody>()
            call.respond(logController.log(body.event, body.type, body.data, session?.id))
        }
    }
}
