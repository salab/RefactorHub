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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ExperimentController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class CreateExperiment

@KtorExperimentalLocationsAPI
@Location("")
class GetAllExperiments

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class GetExperiment(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/refactorings")
data class GetExperimentRefactorings(val id: Int)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.experiments() {
    route("/experiments") {
        val experimentController: ExperimentController by inject()
        post<CreateExperiment> {
            val body = call.receive<ExperimentController.CreateExperimentBody>()
            val session = call.sessions.get<Session>()
            call.respond(experimentController.create(body, session?.id))
        }
        get<GetAllExperiments> {
            call.respond(experimentController.getAll())
        }
        get<GetExperiment> {
            call.respond(experimentController.get(it.id))
        }
        get<GetExperimentRefactorings> {
            call.respond(experimentController.getRefactorings(it.id))
        }
    }
}
