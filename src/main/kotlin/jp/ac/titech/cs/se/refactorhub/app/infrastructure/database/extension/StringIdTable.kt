package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

/**
 * https://qiita.com/earl2/items/366457be58a926b330e0
 * https://stackoverflow.com/questions/61938730/string-primary-key-with-jetbrains-exposed-library-kotlin
 */
open class StringIdTable(tableName: String = "", idColumnName: String, idColumnLength: Int) : IdTable<String>(tableName) {
    override val id: Column<EntityID<String>> = varchar(idColumnName, idColumnLength).uniqueIndex().entityId()
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

abstract class StringEntity(id: EntityID<String>) : Entity<String>(id)
abstract class StringEntityClass<out E: StringEntity>(table: IdTable<String>, entityType: Class<E>? = null) : EntityClass<String, E>(table, entityType)
