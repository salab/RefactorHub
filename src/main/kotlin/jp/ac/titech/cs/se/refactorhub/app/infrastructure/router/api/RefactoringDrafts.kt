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
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringDraftController
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import org.koin.core.component.KoinApiExtension
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
data class PutCodeElementHolder(val id: Int, val category: DiffCategory)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}")
data class RemoveCodeElementHolder(val id: Int, val category: DiffCategory, val key: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}")
data class VerifyCodeElementHolder(val id: Int, val category: DiffCategory, val key: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}")
data class AppendCodeElementDefaultValue(val id: Int, val category: DiffCategory, val key: String)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}/{index}")
data class UpdateCodeElementValue(val id: Int, val category: DiffCategory, val key: String, val index: Int)

@KtorExperimentalLocationsAPI
@Location("/{id}/{category}/{key}/{index}")
data class RemoveCodeElementValue(val id: Int, val category: DiffCategory, val key: String, val index: Int)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.drafts() {
    route("/drafts") {
        val refactoringDraftController: RefactoringDraftController by inject()
        get<GetDraft> {
            call.respond(refactoringDraftController.get(it.id))
        }
        post<SaveDraft> {
            val session = call.sessions.get<Session>()
            call.respond(refactoringDraftController.save(it.id, session?.id))
        }
        post<DiscardDraft> {
            val session = call.sessions.get<Session>()
            call.respond(refactoringDraftController.discard(it.id, session?.id))
        }
        patch<UpdateDraft> {
            val session = call.sessions.get<Session>()
            val body = call.receive<RefactoringDraftController.UpdateDraftBody>()
            call.respond(refactoringDraftController.update(it.id, body, session?.id))
        }
        put<PutCodeElementHolder> {
            val session = call.sessions.get<Session>()
            val body = call.receive<RefactoringDraftController.PutCodeElementHolderBody>()
            call.respond(refactoringDraftController.putCodeElementHolder(it.id, it.category, body, session?.id))
        }
        delete<RemoveCodeElementHolder> {
            val session = call.sessions.get<Session>()
            call.respond(refactoringDraftController.removeCodeElementHolder(it.id, it.category, it.key, session?.id))
        }
        patch<VerifyCodeElementHolder> {
            val session = call.sessions.get<Session>()
            val body = call.receive<RefactoringDraftController.VerifyCodeElementHolderBody>()
            call.respond(
                refactoringDraftController.verifyCodeElementHolder(
                    it.id,
                    it.category,
                    it.key,
                    body.state,
                    session?.id
                )
            )
        }
        post<AppendCodeElementDefaultValue> {
            val session = call.sessions.get<Session>()
            call.respond(
                refactoringDraftController.appendCodeElementDefaultValue(
                    it.id,
                    it.category,
                    it.key,
                    session?.id
                )
            )
        }
        patch<UpdateCodeElementValue> {
            val session = call.sessions.get<Session>()
            val body = call.receive<RefactoringDraftController.UpdateCodeElementValueBody>()
            call.respond(
                refactoringDraftController.updateCodeElementValue(
                    it.id,
                    it.category,
                    it.key,
                    it.index,
                    body,
                    session?.id
                )
            )
        }
        delete<RemoveCodeElementValue> {
            val session = call.sessions.get<Session>()
            call.respond(
                refactoringDraftController.removeCodeElementValue(
                    it.id,
                    it.category,
                    it.key,
                    it.index,
                    session?.id
                )
            )
        }
    }
}
