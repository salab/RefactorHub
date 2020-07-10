package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringDraftController
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class GetDraft(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/save")
data class SaveDraft(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/discard")
data class DiscardDraft(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class UpdateDraft(val id: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}")
data class PutDraftElementKey(val id: Int, val category: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}")
data class RemoveDraftElementKey(val id: Int, val category: String, val key: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}")
data class AppendDraftElementValue(val id: Int, val category: String, val key: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}/{index}")
data class UpdateDraftElementValue(val id: Int, val category: String, val key: String, val index: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}/{index}")
data class RemoveDraftElementValue(val id: Int, val category: String, val key: String, val index: Int)

@KtorExperimentalLocationsAPI
fun Route.drafts() {
    val refactoringDraftController: RefactoringDraftController by inject()
    get<GetDraft> {
        call.respond(refactoringDraftController.get(it.id))
    }
    post<SaveDraft> {
        call.respond(refactoringDraftController.save(it.id))
    }
    post<DiscardDraft> {
        call.respond(refactoringDraftController.discard(it.id))
    }
    patch<UpdateDraft> {
        val body = call.receive<RefactoringDraftController.UpdateDraftBody>()
        call.respond(refactoringDraftController.update(it.id, body))
    }
    put<PutDraftElementKey> {
        val body = call.receive<RefactoringDraftController.PutDraftElementKeyBody>()
        call.respond(refactoringDraftController.putElementKey(it.id, it.category, body))
    }
    delete<RemoveDraftElementKey> {
        call.respond(refactoringDraftController.removeElementKey(it.id, it.category, it.key))
    }
    post<AppendDraftElementValue> {
        call.respond(refactoringDraftController.appendElementValue(it.id, it.category, it.key))
    }
    patch<UpdateDraftElementValue> {
        val body = call.receive<RefactoringDraftController.UpdateDraftElementValueBody>()
        call.respond(refactoringDraftController.updateElementValue(it.id, it.category, it.key, it.index, body))
    }
    delete<RemoveDraftElementValue> {
        call.respond(refactoringDraftController.removeElementValue(it.id, it.category, it.key, it.index))
    }
}
