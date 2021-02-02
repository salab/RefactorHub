package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.app.model.Log
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.LogService
import jp.ac.titech.cs.se.refactorhub.core.model.LogEvent
import jp.ac.titech.cs.se.refactorhub.core.model.LogType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class LogController : KoinComponent {

    data class LogBody(
        val event: LogEvent,
        val type: LogType,
        val data: JsonNode
    )

    private val logService: LogService by inject()

    fun log(event: LogEvent, type: LogType, data: JsonNode, user: Int?): Log {
        return logService.log(event, type, data, user)
    }
}
