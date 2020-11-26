package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import org.jetbrains.exposed.sql.transactions.transaction

class ExperimentRepositoryImpl : ExperimentRepository {
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
