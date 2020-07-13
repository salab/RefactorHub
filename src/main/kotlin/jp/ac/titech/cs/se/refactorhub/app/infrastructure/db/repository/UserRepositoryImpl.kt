package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Users
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.User
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {

    override fun findById(id: Int): User? {
        return transaction {
            UserDao.findById(id)?.asModel()
        }
    }

    override fun findBySubId(subId: Int): User? {
        return transaction {
            UserDao.find {
                Users.subId eq subId
            }.singleOrNull()?.asModel()
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
