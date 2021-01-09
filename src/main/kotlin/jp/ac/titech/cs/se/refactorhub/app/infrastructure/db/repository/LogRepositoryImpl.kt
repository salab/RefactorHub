package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.LogDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.LogRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Log
import org.jetbrains.exposed.sql.transactions.transaction

class LogRepositoryImpl : LogRepository {
    override fun save(log: Log): Log {
        return transaction {
            LogDao.new {
                event = log.event
                type = log.type
                user = log.user
                time = log.time
                data = log.data
            }.asModel()
        }
    }
}
