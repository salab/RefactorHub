package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ActionRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ActionService : KoinComponent {
    private val actionRepository: ActionRepository by inject()

    fun log(name: ActionName, type: ActionType, data: JsonNode, user: Int?) {
        if (LOG_ACTION.toBoolean()) {
            val time = System.currentTimeMillis()
            actionRepository.save(
                Action(name, type, user, time, data)
            )
        }
    }

    companion object {
        private val LOG_ACTION = System.getenv("LOG_ACTION") ?: ""
    }
}
