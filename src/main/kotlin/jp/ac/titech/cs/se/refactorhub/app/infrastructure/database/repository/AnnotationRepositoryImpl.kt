package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.AnnotationRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Annotation
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class AnnotationRepositoryImpl : AnnotationRepository {
    override fun findById(id: UUID): Annotation? {
        return transaction {
            AnnotationDao.findById(id)?.asModel()
        }
    }

    override fun findByOwnerId(ownerId: UUID): List<Annotation> {
        return transaction {
            AnnotationDao.find {
                Annotations.ownerId eq ownerId
            }.map { it.asModel() }
        }
    }

    override fun findByExperimentId(ownerId: UUID, experimentId: UUID): List<Annotation> {
        return transaction {
            AnnotationDao.find {
                (Annotations.ownerId eq ownerId) and
                        (Annotations.experimentId eq experimentId)
            }.map { it.asModel() }
        }
    }

    override fun findByCommitId(ownerId: UUID, experimentId: UUID, commitId: UUID): Annotation? {
        return transaction {
            AnnotationDao.find {
                (Annotations.ownerId eq ownerId) and
                        (Annotations.experimentId eq experimentId) and
                        (Annotations.commitId eq commitId)
            }.firstOrNull()?.asModel()
        }
    }

    override fun create(
        ownerId: UUID,
        commitId: UUID,
        experimentId: UUID,
        isDraft: Boolean,
        hasTemporarySnapshot: Boolean,
        latestInternalCommitSha: String,
        snapshots: List<Snapshot>
    ): Annotation {
        return transaction {
            AnnotationDao.new {
                this.owner = UserDao[ownerId]
                this.commit = CommitDao[commitId]
                this.experiment = ExperimentDao[experimentId]
                this.isDraft = isDraft
                this.hasTemporarySnapshot = hasTemporarySnapshot
                this.latestInternalCommitSha = latestInternalCommitSha
            }.apply {
                this.snapshots = SizedCollection(snapshots.map { SnapshotDao[it.id] })
            }.asModel()
        }
    }

    override fun updateById(
        id: UUID,
        isDraft: Boolean?,
        hasTemporarySnapshot: Boolean?,
        latestInternalCommitSha: String?,
    ): Annotation {
        return transaction {
            val dao = AnnotationDao[id]
            if (isDraft != null) dao.isDraft = isDraft
            if (hasTemporarySnapshot != null) dao.hasTemporarySnapshot = hasTemporarySnapshot
            if (latestInternalCommitSha != null) dao.latestInternalCommitSha = latestInternalCommitSha
            dao.asModel()
        }
    }

    override fun deleteById(id: UUID) {
        transaction {
            AnnotationDao.findById(id)?.delete()
        }
    }
}
