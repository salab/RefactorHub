package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("users") {
    val subId = integer("sub_id").uniqueIndex()
    val name = varchar("name", 50)
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(Users)

    var subId by Users.subId
    var name by Users.name
}
