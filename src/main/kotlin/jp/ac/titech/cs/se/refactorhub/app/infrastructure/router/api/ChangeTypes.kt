package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ChangeTypeController
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class GetChangeTypes

@KtorExperimentalLocationsAPI
@Location("")
class CreateChangeType
data class CreateChangeTypeBody(
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String,
    val referenceUrl: String = ""
)

@KoinApiExtension
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
