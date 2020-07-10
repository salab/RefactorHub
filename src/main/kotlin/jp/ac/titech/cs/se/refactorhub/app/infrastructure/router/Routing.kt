package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CommitController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ElementController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringDraftController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringTypeController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
fun Routing.api() {

    route("/api") {
        users()
        commits()
        refactorings()
        refactoringTypes()
        drafts()
        elements()
    }
}

@KtorExperimentalLocationsAPI
fun Route.users() {
    val userController: UserController by inject()
    get<UserController.GetUser> {
        call.respond(userController.get(it))
    }
    get<UserController.GetUserDrafts> {
        call.respond(userController.getDrafts(it))
    }
    get<UserController.GetUserRefactorings> {
        call.respond(userController.getRefactorings(it))
    }
    get<UserController.GetMe> {
        call.respond(userController.getMe())
    }
}

@KtorExperimentalLocationsAPI
fun Route.commits() {
    val commitController: CommitController by inject()
    get<CommitController.GetCommit> {
        call.respond(commitController.get(it))
    }
    get<CommitController.GetCommitDetail> {
        call.respond(commitController.getDetail(it))
    }
}

@KtorExperimentalLocationsAPI
fun Route.refactorings() {
    val refactoringController: RefactoringController by inject()
    post<RefactoringController.CreateRefactoring> {
        val body = call.receive<RefactoringController.CreateRefactoringBody>()
        call.respond(refactoringController.create(it, body))
    }
    get<RefactoringController.GetAllRefactorings> {
        call.respond(refactoringController.getAll())
    }
    get<RefactoringController.GetRefactoring> {
        call.respond(refactoringController.get(it))
    }
    get<RefactoringController.GetRefactoringChildren> {
        call.respond(refactoringController.getChildren(it))
    }
    get<RefactoringController.GetRefactoringDrafts> {
        call.respond(refactoringController.getDrafts(it))
    }
    post<RefactoringController.ForkRefactoring> {
        call.respond(refactoringController.fork(it))
    }
    post<RefactoringController.EditRefactoring> {
        call.respond(refactoringController.edit(it))
    }
}

@KtorExperimentalLocationsAPI
fun Route.refactoringTypes() {
    val refactoringTypeController: RefactoringTypeController by inject()
    post<RefactoringTypeController.CreateRefactoringType> {
        val body = call.receive<RefactoringTypeController.CreateRefactoringTypeBody>()
        call.respond(refactoringTypeController.create(it, body))
    }
    get<RefactoringTypeController.GetAllRefactoringTypes> {
        call.respond(refactoringTypeController.getAll())
    }
}

@KtorExperimentalLocationsAPI
fun Route.drafts() {
    val refactoringDraftController: RefactoringDraftController by inject()
    get<RefactoringDraftController.GetDraft> {
        call.respond(refactoringDraftController.get(it))
    }
    post<RefactoringDraftController.SaveDraft> {
        call.respond(refactoringDraftController.save(it))
    }
    post<RefactoringDraftController.DiscardDraft> {
        call.respond(refactoringDraftController.discard(it))
    }
    patch<RefactoringDraftController.UpdateDraft> {
        val body = call.receive<RefactoringDraftController.UpdateDraftBody>()
        call.respond(refactoringDraftController.update(it, body))
    }
    put<RefactoringDraftController.PutDraftElementKey> {
        val body = call.receive<RefactoringDraftController.PutDraftElementKeyBody>()
        call.respond(refactoringDraftController.putElementKey(it, body))
    }
    delete<RefactoringDraftController.RemoveDraftElementKey> {
        call.respond(refactoringDraftController.removeElementKey(it))
    }
    post<RefactoringDraftController.AppendDraftElementValue> {
        call.respond(refactoringDraftController.appendElementValue(it))
    }
    patch<RefactoringDraftController.UpdateDraftElementValue> {
        val body = call.receive<RefactoringDraftController.UpdateDraftElementValueBody>()
        call.respond(refactoringDraftController.updateElementValue(it, body))
    }
    delete<RefactoringDraftController.RemoveDraftElementValue> {
        call.respond(refactoringDraftController.removeElementValue(it))
    }
}

@KtorExperimentalLocationsAPI
fun Route.elements() {
    val elementController: ElementController by inject()
    get<ElementController.GetElementTypes> {
        call.respond(elementController.getTypes())
    }
}
