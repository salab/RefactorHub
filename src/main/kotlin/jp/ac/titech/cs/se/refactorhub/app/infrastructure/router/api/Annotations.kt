package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
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
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ActionController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.AnnotationController
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
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
        val actionController: ActionController by inject()
        val mapper: ObjectMapper by inject()

        get<GetAnnotation> {
            val annotationId = it.annotationId.toUUID()
            val annotation = annotationController.getAnnotation(annotationId)

            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            if (userId != null) {
                val actionData = mapper.createObjectNode().apply {
                    put("annotationId", annotationId.toString())
                    put("experimentId", annotation.experimentId.toString())
                    val commitNode = putObject("commit")
                    commitNode.put("repository", annotation.commit.repository)
                    commitNode.put("owner", annotation.commit.owner)
                    commitNode.put("sha", annotation.commit.sha)
                    put("isDraft", annotation.isDraft)
                    put("hasTemporarySnapshot", annotation.hasTemporarySnapshot)
                    replace("snapshots", createSnapshotsNode(mapper, annotation.snapshots))
                }
                actionController.log(ActionName.GetAnnotation, ActionType.Server, actionData, userId)
            }

            call.respond(annotation)
        }
        patch<PublishAnnotation> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val (isDraft) = call.receive<PublishAnnotationBody>()
            val annotation = annotationController.publishAnnotation(annotationId, isDraft, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("isDraft", annotation.isDraft)
            }
            actionController.log(ActionName.PublishAnnotation, ActionType.Server, actionData, userId)

            call.respond(annotation)
        }
        get<GetAnnotationData> {
            val annotationId = it.annotationId.toUUID()
            call.respond(annotationController.getAnnotationData(annotationId))
        }

        post<AppendTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val changeId = call.receive<AppendTemporarySnapshotBody>().changeId.toUUID()
            val annotation = annotationController.appendTemporarySnapshot(annotationId, changeId, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("changeId", changeId.toString())
                put("hasTemporarySnapshot", annotation.hasTemporarySnapshot)
                replace("snapshots", createSnapshotsNode(mapper, annotation.snapshots))
            }
            actionController.log(ActionName.AppendTemporarySnapshot, ActionType.Server, actionData, userId)

            call.respond(annotation)
        }
        put<ModifyTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val (filePath, fileContent, isRemoved) = call.receive<ModifyTemporarySnapshotBody>()
            val annotation = annotationController.modifyTemporarySnapshot(annotationId, filePath, fileContent, isRemoved, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("filePath", filePath)
                put("fileContent", fileContent)
                put("isRemoved", isRemoved)
                replace("snapshots", createSnapshotsNode(mapper, annotation.snapshots))
            }
            actionController.log(ActionName.ModifyTemporarySnapshot, ActionType.Server, actionData, userId)

            call.respond(annotation)
        }
        patch<SettleTemporarySnapshot> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val annotation = annotationController.settleTemporarySnapshot(annotationId, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("hasTemporarySnapshot", annotation.hasTemporarySnapshot)
            }
            actionController.log(ActionName.SettleTemporarySnapshot, ActionType.Server, actionData, userId)

            call.respond(annotation)
        }

        post<AppendChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val snapshotId = it.snapshotId.toUUID()
            val snapshot = annotationController.appendChange(annotationId, snapshotId, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("snapshotId", snapshotId.toString())
                replace("snapshot", createSnapshotNode(mapper, snapshot))
            }
            actionController.log(ActionName.AppendChange, ActionType.Server, actionData, userId)

            call.respond(snapshot)
        }
        patch<UpdateChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (description, typeName) = call.receive<UpdateChangeBody>()
            val change = annotationController.updateChange(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                description,
                typeName,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.UpdateChange, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        delete<RemoveChange> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val annotationId = it.annotationId.toUUID()
            val snapshotId = it.snapshotId.toUUID()
            val changeId = it.changeId.toUUID()
            val annotation = annotationController.removeChange(annotationId, snapshotId, changeId, userId)

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", annotationId.toString())
                put("snapshotId", snapshotId.toString())
                put("changeId", changeId.toString())
                put("hasTemporarySnapshot", annotation.hasTemporarySnapshot)
                replace("snapshots", createSnapshotsNode(mapper, annotation.snapshots))
            }
            actionController.log(ActionName.RemoveChange, ActionType.Server, actionData, userId)

            call.respond(annotation)
        }

        put<PutNewParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (parameterName, elementType, multiple) = call.receive<PutNewParameterBody>()
            val change = annotationController.putNewParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                parameterName,
                elementType,
                multiple,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", parameterName)
                put("codeElementTypeName", elementType)
                put("multiple", multiple)
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.PutNewParameter, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        post<AppendParameterElement> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val change = annotationController.appendParameterElement(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", it.parameterName)
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.AppendParameterElement, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        delete<RemoveParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val change = annotationController.removeParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", it.parameterName)
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.RemoveParameter, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        patch<VerifyParameter> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (isVerified) = call.receive<VerifyParameterBody>()
            val change = annotationController.verifyParameter(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                isVerified,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", it.parameterName)
                put("isVerified", isVerified)
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.VerifyParameter, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        patch<UpdateParameterValue> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val (element) = call.receive<UpdateParameterValueBody>()
            val change = annotationController.updateParameterValue(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                it.elementIndex,
                element,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", it.parameterName)
                put("elementIndex", it.elementIndex)
                replace("element", mapper.valueToTree(element))
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.UpdateParameterValue, ActionType.Server, actionData, userId)

            call.respond(change)
        }
        delete<ClearParameterValue> {
            val userId = call.sessions.get<Session>()?.userId?.toUUID()
            val change = annotationController.clearParameterValue(
                it.annotationId.toUUID(),
                it.snapshotId.toUUID(),
                it.changeId.toUUID(),
                it.diffCategory,
                it.parameterName,
                it.elementIndex,
                userId
            )

            val actionData = mapper.createObjectNode().apply {
                put("annotationId", it.annotationId.toUUID().toString())
                put("snapshotId", it.snapshotId.toUUID().toString())
                put("changeId", it.changeId.toUUID().toString())
                put("diffCategory", it.diffCategory.name)
                put("parameterName", it.parameterName)
                put("elementIndex", it.elementIndex)
                replace("change", createChangeNode(mapper, change))
            }
            actionController.log(ActionName.ClearParameterValue, ActionType.Server, actionData, userId)

            call.respond(change)
        }
    }
}

fun createSnapshotsNode(mapper: ObjectMapper, snapshots: List<Snapshot>): ArrayNode {
    return mapper.createArrayNode().addAll(snapshots.map {snapshot ->
        createSnapshotNode(mapper, snapshot)
    })
}
fun createSnapshotNode(mapper: ObjectMapper, snapshot: Snapshot): ObjectNode {
    val snapshotNode = mapper.createObjectNode()
    snapshotNode.put("snapshotId", snapshot.id.toString())
    snapshotNode.put("patch", snapshot.patch)
    snapshotNode.replace("changes", createChangesNode(mapper, snapshot.changes))
    return snapshotNode
}
fun createChangesNode(mapper: ObjectMapper, changes: List<Change>): ArrayNode {
    return mapper.createArrayNode().addAll(changes.map { change ->
        createChangeNode(mapper, change)
    })
}
fun createChangeNode(mapper: ObjectMapper, change: Change): ObjectNode {
    val changeNode = mapper.createObjectNode()
    changeNode.put("changeId", change.id.toString())
    changeNode.put("typeName", change.typeName)
    changeNode.put("description", change.description)
    changeNode.replace("parameterData", mapper.valueToTree(change.parameterData))
    return changeNode
}
