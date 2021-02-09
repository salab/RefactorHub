package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.ActionDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ActionRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import org.jetbrains.exposed.sql.transactions.transaction

class ActionRepositoryImpl : ActionRepository {
    override fun save(log: Action): Action {
        return transaction {
            ActionDao.new {
                name = log.name
                type = log.type
                user = log.user
                time = log.time
                data = log.data
            }.asModel()
        }
    }
}
