package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ChangeTypeController
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.ktor.ext.inject

@Location("")
class GetChangeTypes

@Location("")
class CreateChangeType
data class CreateChangeTypeBody(
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String,
    val referenceUrl: String = ""
)

@KtorExperimentalLocationsAPI
fun Route.changeTypes() {
    route("/change_types") {
        val changeTypeController: ChangeTypeController by inject()

        get<GetChangeTypes> {
            call.respond(changeTypeController.getAll())
        }
        post<CreateChangeType> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (name, before, after, description, referenceUrl) = call.receive<CreateChangeTypeBody>()
            call.respond(changeTypeController.create(name, userId, before, after, description, referenceUrl))
        }
    }
}
