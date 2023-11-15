package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ChangeTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ChangeTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ChangeTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.ChangeType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ChangeTypeRepositoryImpl : ChangeTypeRepository {
    override fun findByName(name: String): ChangeType? {
        return transaction {
            ChangeTypeDao.find {
                ChangeTypes.id eq name
            }.firstOrNull()?.asModel()
        }
    }

    override fun findAll(): List<ChangeType> {
        return transaction {
            ChangeTypeDao.all().map {
                it.asModel()
            }
        }
    }

    override fun create(
        name: String,
        ownerId: UUID,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String,
        referenceUrl: String,
    ): ChangeType {
        return transaction {
            ChangeTypeDao.new(name) {
                this.owner = UserDao[ownerId]
                this.before = before
                this.after = after
                this.description = description
                this.referenceUrl = referenceUrl
            }.asModel()
        }
    }

    override fun deleteByName(name: String) {
        transaction {
            ChangeTypeDao.findById(name)?.delete()
        }
    }
}
