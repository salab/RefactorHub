package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Experiments : UUIDTable("experiments") {
    val owner = reference("owner_id", Users)
    val title = varchar("title", 100)
    val description = text("description")
    val isActive = bool("is_active").default(false)
}

object ExperimentsToCommits : Table("experiments_to_commits") {
    val experiment = reference("experiment_id", Experiments)
    val commit = reference("commit_id", Commits)
    override val primaryKey = PrimaryKey(experiment, commit)
}

class ExperimentDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Experiment> {
    companion object : UUIDEntityClass<ExperimentDao>(Experiments)

    var owner by UserDao referencedOn Experiments.owner
    var title by Experiments.title
    var description by Experiments.description
    var isActive by Experiments.isActive
    var targetCommits by CommitDao via ExperimentsToCommits

    @OptIn(ExperimentalStdlibApi::class)
    override fun asModel(): Experiment {
        return Experiment(
            this.id.value,
            this.owner.asModel().id,
            this.title,
            this.description,
            this.isActive,
            this.targetCommits.sortedBy { "${it.owner}/${it.repository}/${it.sha}".lowercase() }.map { it.asModel() }
        )
    }
}
