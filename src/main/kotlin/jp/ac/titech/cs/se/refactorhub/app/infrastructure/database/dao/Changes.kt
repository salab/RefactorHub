package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.koin.java.KoinJavaComponent.inject
import java.util.UUID

object Changes : UUIDTable("changes") {
    val snapshotId = reference("snapshot_id", Snapshots)
    val orderIndex = integer("order_index")
    val typeName = reference("type_name", ChangeTypes)
    val description = text("description")
    val parameterData = jsonb("parameter_data", ::stringifyParameterData, ::parseParameterData)
}

class ChangeDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Change> {
    companion object : UUIDEntityClass<ChangeDao>(Changes)

    var snapshot by SnapshotDao referencedOn Changes.snapshotId
    var orderIndex by Changes.orderIndex
    var type by ChangeTypeDao referencedOn Changes.typeName
    var description by Changes.description
    var parameterData by Changes.parameterData

    override fun asModel() = Change(
        this.id.value,
        this.orderIndex,
        this.type.name,
        this.description,
        this.parameterData
    )
}

private fun stringifyParameterData(data: Change.ParameterData): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}

private fun parseParameterData(src: String): Change.ParameterData {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
