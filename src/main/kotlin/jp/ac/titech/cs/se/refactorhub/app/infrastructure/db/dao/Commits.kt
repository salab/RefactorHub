package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Commits : IntIdTable("commits") {
    val sha = varchar("sha", 40).uniqueIndex()
    val owner = varchar("owner", 100)
    val repository = varchar("repository", 100)
}

class CommitDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CommitDao>(Commits)

    var sha by Commits.sha
    var owner by Commits.owner
    var repository by Commits.repository
}
