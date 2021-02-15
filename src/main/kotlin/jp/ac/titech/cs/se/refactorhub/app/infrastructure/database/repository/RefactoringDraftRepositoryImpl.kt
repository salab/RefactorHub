package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDraftDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDrafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringDraftRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class RefactoringDraftRepositoryImpl : RefactoringDraftRepository {
    override fun findById(id: Int): RefactoringDraft? {
        return transaction {
            RefactoringDraftDao.findById(id)?.asModel()
        }
    }

    override fun findByOwnerId(ownerId: Int): List<RefactoringDraft> {
        return transaction {
            RefactoringDraftDao.find {
                RefactoringDrafts.owner eq ownerId
            }.map {
                it.asModel()
            }
        }
    }

    override fun findByRefactoringId(refactoringId: Int): List<RefactoringDraft> {
        return transaction {
            RefactoringDraftDao.find {
                RefactoringDrafts.origin eq refactoringId
            }.map {
                it.asModel()
            }
        }
    }

    override fun findByRefactoringIdAndOwnerIdAndIsFork(
        refactoringId: Int,
        ownerId: Int,
        isFork: Boolean
    ): RefactoringDraft? {
        return transaction {
            RefactoringDraftDao.find {
                (RefactoringDrafts.origin eq refactoringId) and
                    (RefactoringDrafts.owner eq ownerId) and
                    (RefactoringDrafts.isFork eq isFork)
            }.firstOrNull()?.asModel()
        }
    }

    override fun create(
        commit: Commit,
        typeName: String,
        data: Refactoring.Data,
        description: String,
        userId: Int,
        originId: Int,
        isFork: Boolean
    ): RefactoringDraft {
        return transaction {
            RefactoringDraftDao.new {
                this.owner = UserDao.findById(userId)!!
                this.origin = RefactoringDao.find { Refactorings.id eq originId }.first()
                this.isFork = isFork
                this.commit = CommitDao.find {
                    (Commits.owner eq commit.owner) and
                        (Commits.repository eq commit.repository) and
                        (Commits.sha eq commit.sha)
                }.first()
                this.type = RefactoringTypeDao.find { RefactoringTypes.name eq typeName }.first()
                this.data = data
                this.description = description
            }.asModel()
        }
    }

    override fun updateById(
        id: Int,
        typeName: String?,
        data: Refactoring.Data?,
        description: String?
    ): RefactoringDraft {
        return transaction {
            val dao = RefactoringDraftDao.findById(id)!!
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
            RefactoringDraftDao.findById(id)?.delete()
        }
    }
}
