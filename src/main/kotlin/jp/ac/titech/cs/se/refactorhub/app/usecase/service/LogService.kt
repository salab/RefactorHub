package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.LogRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Log
import jp.ac.titech.cs.se.refactorhub.core.model.LogEvent
import jp.ac.titech.cs.se.refactorhub.core.model.LogType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class LogService : KoinComponent {
    private val logRepository: LogRepository by inject()

    fun log(event: LogEvent, type: LogType, data: JsonNode, user: Int?): Log {
        val time = System.currentTimeMillis()
        return logRepository.save(
            Log(event, type, user ?: 0, time, data)
        )
    }
}
