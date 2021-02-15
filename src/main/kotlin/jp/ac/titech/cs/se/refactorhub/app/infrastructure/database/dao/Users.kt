package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable("users") {
    val subId = integer("sub_id").uniqueIndex()
    val name = varchar("name", 50)
}

class UserDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<User> {
    companion object : IntEntityClass<UserDao>(Users)

    var subId by Users.subId
    var name by Users.name

    override fun asModel(): User {
        return User(
            this.id.value,
            this.name
        )
    }
}
