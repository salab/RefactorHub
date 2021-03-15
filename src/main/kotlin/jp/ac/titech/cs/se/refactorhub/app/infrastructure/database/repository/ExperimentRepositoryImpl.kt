package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

class ExperimentRepositoryImpl : ExperimentRepository {
    override fun create(title: String, description: String, refactorings: List<Refactoring>, userId: Int): Experiment {
        return transaction {
            ExperimentDao.new {
                this.owner = UserDao[userId]
                this.title = title
                this.description = description
            }.apply {
                this.refactorings = SizedCollection(refactorings.map { RefactoringDao[it.id] })
            }.asModel()
        }
    }

    override fun findAll(): List<Experiment> {
        return transaction {
            ExperimentDao.all().map { it.asModel() }
        }
    }

    override fun findById(id: Int): Experiment? {
        return transaction {
            ExperimentDao.findById(id)?.asModel()
        }
    }
}
