package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Users : UUIDTable("users") {
    /** value of `(org.kohsuke.github.GHMyself).id.toInt()` */
    val subId = integer("sub_id").uniqueIndex()
    val name = varchar("name", 50)
}

class UserDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<User> {
    companion object : UUIDEntityClass<UserDao>(Users)

    var subId by Users.subId
    var name by Users.name
    val annotations by AnnotationOverviewDao referrersOn Annotations.ownerId

    override fun asModel(): User {
        return User(
            this.id.value,
            this.name
        )
    }
}
