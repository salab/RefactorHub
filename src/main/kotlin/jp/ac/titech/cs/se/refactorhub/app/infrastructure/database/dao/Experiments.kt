package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Experiments : IntIdTable("experiments") {
    val owner = reference("owner", Users)
    val title = varchar("title", 100)
    val description = text("description")
    val isActive = bool("is_active").default(false)
}

object ExperimentRefactorings : Table("experiment_refactorings") {
    val experiment = reference("experiment", Experiments)
    val refactoring = reference("refactoring", Refactorings)
    override val primaryKey = PrimaryKey(experiment, refactoring)
}

class ExperimentDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<Experiment> {
    companion object : IntEntityClass<ExperimentDao>(Experiments)

    var owner by UserDao referencedOn Experiments.owner
    var title by Experiments.title
    var description by Experiments.description
    var isActive by Experiments.isActive
    var refactorings by RefactoringDao via ExperimentRefactorings

    override fun asModel(): Experiment {
        return Experiment(
            this.id.value,
            this.owner.id.value,
            this.title,
            this.description,
            this.isActive
        )
    }
}
