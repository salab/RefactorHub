package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExperimentRepositoryImpl : ExperimentRepository {
    override fun findById(id: UUID): Experiment? {
        return transaction {
            ExperimentDao.findById(id)?.asModel()
        }
    }

    override fun findAll(): List<Experiment> {
        return transaction {
            ExperimentDao.all().map { it.asModel() }
        }
    }

    override fun create(
        ownerId: UUID,
        title: String,
        description: String,
        isActive: Boolean,
    ): Experiment {
        return transaction {
            ExperimentDao.new {
                this.owner = UserDao[ownerId]
                this.title = title
                this.description = description
                this.isActive = isActive
            }.asModel()
        }
    }
}
