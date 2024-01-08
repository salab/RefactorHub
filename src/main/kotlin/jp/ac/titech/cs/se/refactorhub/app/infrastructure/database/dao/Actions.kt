package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.koin.java.KoinJavaComponent.inject
import java.time.LocalDateTime

object Actions : LongIdTable("actions") {
    val name = enumerationByName("name", 50, ActionName::class)
    val type = enumerationByName("type", 50, ActionType::class)
    val userId = reference("user_id", Users)
    val time = varchar("time", 50)
    val data = jsonb("data", ::stringify, ::parse)
}

class ActionDao(id: EntityID<Long>) : LongEntity(id), ModelConverter<Action> {
    companion object : LongEntityClass<ActionDao>(Actions)

    var name by Actions.name
    var type by Actions.type
    var user by UserDao referencedOn Actions.userId
    var time by Actions.time
    var data by Actions.data

    override fun asModel(): Action {
        return Action(
            this.name,
            this.type,
            this.user.id.value,
            LocalDateTime.parse(this.time),
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
