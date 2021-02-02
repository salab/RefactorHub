package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Log
import jp.ac.titech.cs.se.refactorhub.core.model.LogEvent
import jp.ac.titech.cs.se.refactorhub.core.model.LogType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent.inject

object Logs : IntIdTable("logs") {
    val event = enumerationByName("event", 50, LogEvent::class)
    val type = enumerationByName("type", 50, LogType::class)
    val user = integer("user")
    val time = long("time")
    val data = jsonb("data", ::stringify, ::parse)
}

class LogDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<Log> {
    companion object : IntEntityClass<LogDao>(Logs)

    var event by Logs.event
    var type by Logs.type
    var user by Logs.user
    var time by Logs.time
    var data by Logs.data

    override fun asModel(): Log {
        return Log(
            this.event,
            this.type,
            this.user,
            this.time,
            this.data
        )
    }
}

private fun stringify(data: JsonNode): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}

private fun parse(src: String): JsonNode {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
