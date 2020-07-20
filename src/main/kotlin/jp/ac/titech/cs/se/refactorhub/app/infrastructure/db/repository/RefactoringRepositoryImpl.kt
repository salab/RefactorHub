package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.jetbrains.exposed.sql.transactions.transaction

class RefactoringRepositoryImpl : RefactoringRepository {
    override fun findAll(): List<Refactoring> {
        return transaction {
            RefactoringDao.all().map {
                it.asModel()
            }
        }
    }

    override fun findById(id: Int): Refactoring? {
        return transaction {
            RefactoringDao.findById(id)?.asModel()
        }
    }

    override fun findByOwnerId(ownerId: Int): List<Refactoring> {
        return transaction {
            RefactoringDao.find {
                Refactorings.owner eq ownerId
            }.map {
                it.asModel()
            }
        }
    }

    override fun findByParentId(id: Int): List<Refactoring> {
        return transaction {
            RefactoringDao.findById(id)?.children?.map {
                it.asModel()
            } ?: emptyList()
        }
    }

    override fun create(
        commitSha: String,
        typeName: String,
        data: Refactoring.Data,
        description: String,
        userId: Int,
        parentId: Int?
    ): Refactoring {
        return transaction {
            val dao = RefactoringDao.new {
                this.owner = UserDao.findById(userId)!!
                this.commit = CommitDao.find { Commits.sha eq commitSha }.single()
                this.type = RefactoringTypeDao.find { RefactoringTypes.name eq typeName }.single()
                this.data = data
                this.description = description
            }
            dao.parents = RefactoringDao.find { Refactorings.id eq parentId }
            dao.asModel()
        }
    }

    override fun update(id: Int, typeName: String?, data: Refactoring.Data?, description: String?): Refactoring {
        return transaction {
            val dao = RefactoringDao.findById(id)!!
            if (typeName != null) {
                val type = RefactoringTypeDao.find { RefactoringTypes.name eq typeName }.singleOrNull()
                if (type != null) dao.type = type
            }
            if (data != null) dao.data = data
            if (description != null) dao.description = description
            dao.asModel()
        }
    }

    override fun deleteById(id: Int) {
        transaction {
            RefactoringDao.findById(id)?.delete()
        }
    }
}
