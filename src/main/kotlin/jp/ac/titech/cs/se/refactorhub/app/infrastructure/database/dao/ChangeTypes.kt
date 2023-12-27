package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.StringEntity
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.StringEntityClass
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.StringIdTable
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.ChangeType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.java.KoinJavaComponent.inject

object ChangeTypes : StringIdTable("change_types", "name", 100) {
    val ownerId = reference("owner_id", Users)
    val before = jsonb("before", ::stringifyParameterMap, ::parseParameterMap)
    val after = jsonb("after", ::stringifyParameterMap, ::parseParameterMap)
    val description = text("description")
    val referenceUrl = varchar("reference_url", 500)
    val tags = jsonb("tags", ::stringifyTags, ::parseTags)
}

class ChangeTypeDao(id: EntityID<String>) : StringEntity(id), ModelConverter<ChangeType> {
    companion object : StringEntityClass<ChangeTypeDao>(ChangeTypes)

    var owner by UserDao referencedOn ChangeTypes.ownerId
    var before by ChangeTypes.before
    var after by ChangeTypes.after
    var description by ChangeTypes.description
    var referenceUrl by ChangeTypes.referenceUrl
    var tags by ChangeTypes.tags

    val name = id.value

    override fun asModel() = ChangeType(
        this.name,
        this.owner.id.value,
        this.before,
        this.after,
        this.description,
        this.referenceUrl,
        this.tags,
    )
}

private fun stringifyParameterMap(data: Map<String, CodeElementMetadata>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseParameterMap(src: String): Map<String, CodeElementMetadata> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}

private fun stringifyTags(data: List<String>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseTags(src: String): List<String> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
