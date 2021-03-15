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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringTypeController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class CreateRefactoringType

@KtorExperimentalLocationsAPI
@Location("")
class GetAllRefactoringTypes

@KtorExperimentalLocationsAPI
@Location("/{name}")
class GetRefactoringType(val name: String)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.refactoringTypes() {
    route("/refactoring_types") {
        val refactoringTypeController: RefactoringTypeController by inject()
        post<CreateRefactoringType> {
            val body = call.receive<RefactoringTypeController.CreateRefactoringTypeBody>()
            val session = call.sessions.get<Session>()
            call.respond(refactoringTypeController.create(body, session?.id))
        }
        get<GetAllRefactoringTypes> {
            call.respond(refactoringTypeController.getAll())
        }
        get<GetRefactoringType> {
            call.respond(refactoringTypeController.get(it.name))
        }
    }
}
