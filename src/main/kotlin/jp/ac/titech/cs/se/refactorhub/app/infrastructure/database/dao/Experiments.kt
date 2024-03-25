package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Experiments : UUIDTable("experiments") {
    val owner = reference("owner_id", Users)
    val title = varchar("title", 100)
    val description = text("description")
    val isActive = bool("is_active").default(false)
}

class ExperimentDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Experiment> {
    companion object : UUIDEntityClass<ExperimentDao>(Experiments)

    var owner by UserDao referencedOn Experiments.owner
    var title by Experiments.title
    var description by Experiments.description
    var isActive by Experiments.isActive
    val targetCommits by CommitOverviewDao referrersOn Commits.experimentId

    override fun asModel(): Experiment {
        return Experiment(
            this.id.value,
            this.owner.id.value,
            this.title,
            this.description,
            this.isActive,
            this.targetCommits.sortedBy { it.orderIndex }.map { it.asModel() }
        )
    }
}
