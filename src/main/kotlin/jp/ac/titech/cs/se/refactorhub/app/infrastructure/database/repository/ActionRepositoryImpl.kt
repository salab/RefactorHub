package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ActionDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ActionRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import org.jetbrains.exposed.sql.transactions.transaction

class ActionRepositoryImpl : ActionRepository {
    override fun save(action: Action): Action {
        return transaction {
            ActionDao.new {
                name = action.name
                type = action.type
                user = action.user
                time = action.time
                data = action.data
            }.asModel()
        }
    }
}
