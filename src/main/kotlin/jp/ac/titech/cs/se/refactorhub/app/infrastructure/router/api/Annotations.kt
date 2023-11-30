package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.toUUID
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.AnnotationController
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/{annotationId}")
class GetAnnotation(val annotationId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}")
class PublishAnnotation(val annotationId: String)
data class PublishAnnotationBody(val isDraft: Boolean)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/data")
class GetAnnotationData(val annotationId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots")
class AppendTemporarySnapshot(val annotationId: String)
data class AppendTemporarySnapshotBody(val changeId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots")
class ModifyTemporarySnapshot(val annotationId: String)
data class ModifyTemporarySnapshotBody(val filePath: String, val fileContent: String, val isRemoved: Boolean)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots")
class SettleTemporarySnapshot(val annotationId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes")
class AppendChange(val annotationId: String, val snapshotId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}")
class UpdateChange(val annotationId: String, val snapshotId: String, val changeId: String)
data class UpdateChangeBody(val description: String, val typeName: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}")
class RemoveChange(val annotationId: String, val snapshotId: String, val changeId: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}")
class PutNewParameter(val annotationId: String, val snapshotId: String, val changeId: String,
                      val diffCategory: DiffCategory)
data class PutNewParameterBody(val parameterName: String, val elementType: String, val multiple: Boolean)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}/{parameterName}")
class AppendParameterElement(val annotationId: String, val snapshotId: String, val changeId: String,
                             val diffCategory: DiffCategory, val parameterName: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}/{parameterName}")
class RemoveParameter(val annotationId: String, val snapshotId: String, val changeId: String,
                      val diffCategory: DiffCategory, val parameterName: String)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}/{parameterName}")
class VerifyParameter(val annotationId: String, val snapshotId: String, val changeId: String,
                      val diffCategory: DiffCategory, val parameterName: String)
data class VerifyParameterBody(val isVerified: Boolean)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}/{parameterName}/{elementIndex}")
class UpdateParameterValue(val annotationId: String, val snapshotId: String, val changeId: String,
                           val diffCategory: DiffCategory, val parameterName: String, val elementIndex: Int)
data class UpdateParameterValueBody(val element: CodeElement)

@KtorExperimentalLocationsAPI
@Location("/{annotationId}/snapshots/{snapshotId}/changes/{changeId}/{diffCategory}/{parameterName}/{elementIndex}")
class ClearParameterValue(val annotationId: String, val snapshotId: String, val changeId: String,
                          val diffCategory: DiffCategory, val parameterName: String, val elementIndex: Int)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.annotations() {
    route("/annotations") {
        val annotationController: AnnotationController by inject()

        get<GetAnnotation> {
            val annotationId = it.annotationId.toUUID()
            call.respond(annotationController.getAnnotation(annotationId))
        }
        patch<PublishAnnotation> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val (isDraft) = call.receive<PublishAnnotationBody>()
            call.respond(annotationController.publishAnnotation(annotationId, isDraft, userId))
        }
        post<GetAnnotationData> {
            val annotationId = it.annotationId.toUUID()
            call.respond(annotationController.getAnnotationData(annotationId))
        }

        post<AppendTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val changeId = call.receive<AppendTemporarySnapshotBody>().changeId.toUUID()
            call.respond(annotationController.appendTemporarySnapshot(annotationId, changeId, userId))
        }
        put<ModifyTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val (filePath, fileContent, isRemoved) = call.receive<ModifyTemporarySnapshotBody>()
            call.respond(annotationController.modifyTemporarySnapshot(annotationId, filePath, fileContent, isRemoved, userId))
        }
        patch<SettleTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            call.respond(annotationController.settleTemporarySnapshot(annotationId, userId))
        }

        post<AppendChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val snapshotId = it.snapshotId.toUUID()
            call.respond(annotationController.appendChange(annotationId, snapshotId, userId))
        }
        patch<UpdateChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (description, typeName) = call.receive<UpdateChangeBody>()
            call.respond(annotationController.updateChange(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                description,
                typeName,
                userId
            ))
        }
        delete<RemoveChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val snapshotId = it.snapshotId.toUUID()
            val changeId = it.changeId.toUUID()
            call.respond(annotationController.removeChange(annotationId, snapshotId, changeId, userId))
        }

        put<PutNewParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (parameterName, elementType, multiple) = call.receive<PutNewParameterBody>()
            call.respond(annotationController.putNewParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                parameterName,
                elementType,
                multiple,
                userId
            ))
        }
        post<AppendParameterElement> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            call.respond(annotationController.appendParameterElement(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                userId
            ))
        }
        delete<RemoveParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            call.respond(annotationController.removeParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                userId
            ))
        }
        patch<VerifyParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (isVerified) = call.receive<VerifyParameterBody>()
            call.respond(annotationController.verifyParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                isVerified,
                userId
            ))
        }
        patch<UpdateParameterValue> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (element) = call.receive<UpdateParameterValueBody>()
            call.respond(annotationController.updateParameterValue(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                it.elementIndex,
                element,
                userId
            ))
        }
        delete<ClearParameterValue> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            call.respond(annotationController.clearParameterValue(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                it.elementIndex,
                userId
            ))
        }
    }
}
