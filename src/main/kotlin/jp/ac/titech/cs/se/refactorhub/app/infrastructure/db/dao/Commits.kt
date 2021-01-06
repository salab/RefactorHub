package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Commits : IntIdTable("commits") {
    val sha = varchar("sha", 40).uniqueIndex()
    val owner = varchar("owner", 100)
    val repository = varchar("repository", 100)
    val parent = varchar("parent", 40).uniqueIndex()
}

class CommitDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<Commit> {
    companion object : IntEntityClass<CommitDao>(Commits)

    var sha by Commits.sha
    var owner by Commits.owner
    var repository by Commits.repository
    var parent by Commits.parent

    override fun asModel(): Commit {
        return Commit(
            this.sha,
            this.owner,
            this.repository,
            this.parent
        )
    }
}
