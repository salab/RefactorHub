package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import com.fasterxml.jackson.databind.JsonNode
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
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ActionController
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class PostAction
data class PostActionBody(
    val name: ActionName,
    val type: ActionType,
    val data: JsonNode
)

@KoinApiExtension
@KtorExperimentalLocationsAPI
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
