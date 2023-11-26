package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ChangeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ChangeRepositoryImpl : ChangeRepository {
    override fun findById(id: UUID): Change? {
        return transaction {
            ChangeDao.findById(id)?.asModel()
        }
    }

    override fun create(
        typeName: String,
        description: String,
        parameterData: Change.ParameterData
    ): Change {
        return transaction {
            ChangeDao.new {
                this.type = ChangeTypeDao.find { ChangeTypes.id eq typeName }.first()
                this.parameterData = parameterData
                this.description = description
            }.asModel()
        }
    }

    override fun updateById(
        id: UUID,
        typeName: String?,
        description: String?,
        parameterData: Change.ParameterData?
    ): Change {
        return transaction {
            val dao = ChangeDao[id]
            if (typeName != null) {
                val type = ChangeTypeDao.find { ChangeTypes.id eq typeName }.firstOrNull()
                if (type != null) dao.type = type
            }
            if (description != null) dao.description = description
            if (parameterData != null) dao.parameterData = parameterData
            dao.asModel()
        }
    }

    override fun deleteById(id: UUID) {
        transaction {
            ChangeDao.findById(id)?.delete()
        }
    }
}
