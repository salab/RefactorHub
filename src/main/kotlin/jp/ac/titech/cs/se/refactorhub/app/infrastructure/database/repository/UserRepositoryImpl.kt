package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Users
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    override fun findById(id: UUID): User? {
        return transaction {
            UserDao.findById(id)?.asModel()
        }
    }

    override fun findBySubId(subId: Int): User? {
        return transaction {
            UserDao.find {
                Users.subId eq subId
            }.firstOrNull()?.asModel()
        }
    }

    override fun create(subId: Int, name: String): User {
        return transaction {
            UserDao.new {
                this.subId = subId
                this.name = name
            }.asModel()
        }
    }
}
