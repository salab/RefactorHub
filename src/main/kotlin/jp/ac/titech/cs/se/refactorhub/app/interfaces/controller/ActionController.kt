package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ActionService
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class ActionController : KoinComponent {
    private val actionService: ActionService by inject()

    fun log(name: ActionName, type: ActionType, data: JsonNode, userId: UUID?) {
        return actionService.log(name, type, data, userId)
    }
}
