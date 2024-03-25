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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ExperimentController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class GetExperiments

@KtorExperimentalLocationsAPI
@Location("")
class CreateExperiment
data class Commit(
    override val owner: String,
    override val repository: String,
    override val sha: String
): jp.ac.titech.cs.se.refactorhub.core.model.Commit
data class CreateExperimentBody(
    val title: String,
    val description: String,
    val commits: List<Commit>
)

@KtorExperimentalLocationsAPI
@Location("/{experimentId}")
data class GetExperiment(val experimentId: String)

@KtorExperimentalLocationsAPI
@Location("/{experimentId}/commits/{commitId}")
data class StartAnnotation(val experimentId: String, val commitId: String)

@KtorExperimentalLocationsAPI
@Location("/{experimentId}/result")
data class GetExperimentResult(val experimentId: String)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.experiments() {
    route("/experiments") {
        val experimentController: ExperimentController by inject()

        get<GetExperiments> {
            call.respond(experimentController.getAll())
        }
        post<CreateExperiment> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (title, description, commits) = call.receive<CreateExperimentBody>()
            call.respond(experimentController.create(userId, title, description, true, commits))
        }
        get<GetExperiment> {
            val experimentId = it.experimentId.toUUID()
            call.respond(experimentController.get(experimentId))
        }
        post<StartAnnotation> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val experimentId = it.experimentId.toUUID()
            val commitId = it.commitId.toUUID()
            call.respond(experimentController.startAnnotation(userId, experimentId, commitId))
        }
        get<GetExperimentResult> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val experimentId = it.experimentId.toUUID()
            call.respond(experimentController.getExperimentResult(userId, experimentId))
        }
    }
}
