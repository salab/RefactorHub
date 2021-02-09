package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ActionService
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ActionController : KoinComponent {

    data class ActionBody(
        val name: ActionName,
        val type: ActionType,
        val data: JsonNode
    )

    private val actionService: ActionService by inject()

    fun log(name: ActionName, type: ActionType, data: JsonNode, user: Int?): Action {
        return actionService.log(name, type, data, user)
    }
}
