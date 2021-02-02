package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.sql.transactions.transaction

class RefactoringTypeRepositoryImpl : RefactoringTypeRepository {
    override fun findAll(): List<RefactoringType> {
        return transaction {
            RefactoringTypeDao.all().map {
                it.asModel()
            }
        }
    }

    override fun findById(id: Int): RefactoringType? {
        return transaction {
            RefactoringTypeDao.findById(id)?.asModel()
        }
    }

    override fun findByName(name: String): RefactoringType? {
        return transaction {
            RefactoringTypeDao.find {
                RefactoringTypes.name eq name
            }.singleOrNull()?.asModel()
        }
    }

    override fun deleteById(id: Int) {
        transaction {
            RefactoringTypeDao.findById(id)?.delete()
        }
    }

    override fun create(
        name: String,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String
    ): RefactoringType {
        return transaction {
            RefactoringTypeDao.new {
                this.name = name
                this.before = before
                this.after = after
                this.description = description
            }.asModel()
        }
    }
}
