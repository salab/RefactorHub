package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ChangeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Change
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
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
                this.description = description
                this.parameterData = parameterData.deepCopy()
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
            if (parameterData != null) dao.parameterData = parameterData.deepCopy()
            dao.asModel()
        }
    }

    override fun deleteById(id: UUID) {
        transaction {
            ChangeDao.findById(id)?.delete()
        }
    }
}

private fun Change.ParameterData.deepCopy(): Change.ParameterData {
    return Change.ParameterData(
        this@deepCopy.before.deepCopy(),
        this@deepCopy.after.deepCopy()
    )
}
private fun MutableMap<String, CodeElementHolder>.deepCopy(): MutableMap<String, CodeElementHolder> {
    return this.map {
        it.key to it.value.let { holder ->
            holder.copy(elements = ArrayList(holder.elements))
        }
    }.toMap().toMutableMap()
}
