package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class RefactoringRepositoryImpl : RefactoringRepository {

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

    override fun findByExperimentId(experimentId: Int): List<Refactoring> {
        return transaction {
            ExperimentDao.findById(experimentId)?.refactorings?.map { it.asModel() } ?: emptyList()
        }
    }

    override fun create(
        commit: Commit,
        typeName: String,
        data: Refactoring.Data,
        description: String,
        userId: Int,
        parentId: Int?,
        isVerified: Boolean
    ): Refactoring {
        return transaction {
            val dao = RefactoringDao.new {
                this.owner = UserDao.findById(userId)!!
                this.commit = CommitDao.find {
                    (Commits.owner eq commit.owner) and
                        (Commits.repository eq commit.repository) and
                        (Commits.sha eq commit.sha)
                }.first()
                this.type = RefactoringTypeDao.find { RefactoringTypes.name eq typeName }.first()
                this.data = data
                this.description = description
                this.isVerified = isVerified
            }
            dao.parents = RefactoringDao.find { Refactorings.id eq parentId }
            dao.asModel()
        }
    }

    override fun updateById(id: Int, typeName: String?, data: Refactoring.Data?, description: String?): Refactoring {
        return transaction {
            val dao = RefactoringDao.findById(id)!!
            if (typeName != null) {
                val type = RefactoringTypeDao.find { RefactoringTypes.name eq typeName }.firstOrNull()
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
