package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Commits : IntIdTable("commits") {
    val owner = varchar("owner", 100)
    val repository = varchar("repository", 100)
    val sha = varchar("sha", 40)
}

class CommitDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<Commit> {
    companion object : IntEntityClass<CommitDao>(Commits)

    var owner by Commits.owner
    var repository by Commits.repository
    var sha by Commits.sha

    override fun asModel(): Commit {
        return Commit(
            this.owner,
            this.repository,
            this.sha,
        )
    }
}
