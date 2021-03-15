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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringController
import jp.ac.titech.cs.se.refactorhub.app.model.CreateRefactoringBody
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("")
class CreateRefactoring

@KtorExperimentalLocationsAPI
@Location("")
class GetAllRefactorings

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class GetRefactoring(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/children")
data class GetRefactoringChildren(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/drafts")
data class GetRefactoringDrafts(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/fork")
data class ForkRefactoring(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/edit")
data class EditRefactoring(val id: Int)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.refactorings() {
    route("/refactorings") {
        val refactoringController: RefactoringController by inject()
        post<CreateRefactoring> {
            val body = call.receive<CreateRefactoringBody>()
            val session = call.sessions.get<Session>()
            call.respond(refactoringController.create(body, session?.id))
        }
        get<GetAllRefactorings> {
            call.respond(refactoringController.getAll())
        }
        get<GetRefactoring> {
            call.respond(refactoringController.get(it.id))
        }
        get<GetRefactoringChildren> {
            call.respond(refactoringController.getChildren(it.id))
        }
        get<GetRefactoringDrafts> {
            call.respond(refactoringController.getDrafts(it.id))
        }
        post<ForkRefactoring> {
            val session = call.sessions.get<Session>()
            call.respond(refactoringController.fork(it.id, session?.id))
        }
        post<EditRefactoring> {
            val session = call.sessions.get<Session>()
            call.respond(refactoringController.edit(it.id, session?.id))
        }
    }
}
