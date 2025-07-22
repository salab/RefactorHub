package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ActionController
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.koin.ktor.ext.inject

@Location("")
class PostAction
data class PostActionBody(
    val name: ActionName,
    val type: ActionType,
    val data: JsonNode
)

fun Route.actions() {
    route("/actions") {
        val actionController: ActionController by inject()

        post<PostAction> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (name, type, data) = call.receive<PostActionBody>()
            call.respond(actionController.log(name, type, data, userId))
        }
    }
}
