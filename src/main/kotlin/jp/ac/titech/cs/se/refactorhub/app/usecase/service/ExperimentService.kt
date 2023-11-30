package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class ExperimentService : KoinComponent {
    private val commitService: CommitService by inject()
    private val userService: UserService by inject()
    private val experimentRepository: ExperimentRepository by inject()

    fun get(experimentId: UUID): Experiment {
        val experiment = experimentRepository.findById(experimentId)
        experiment ?: throw NotFoundException("Experiment(id=$experimentId) is not found")
        return experiment
    }

    fun getAll(): List<Experiment> {
        return experimentRepository.findAll()
    }

    fun verifyIds(experimentId: UUID, commitId: UUID?) {
        val experiment = get(experimentId)
        if (commitId == null) return
        if (experiment.targetCommits.find { it.id == commitId } == null)
            throw NotFoundException("Commit(id=$commitId) is not found")
    }

    fun create(
        ownerId: UUID?,
        title: String,
        description: String,
        isActive: Boolean,
        targetCommits: List<Commit>
    ): Experiment {
        val owner = userService.getMe(ownerId)
        return experimentRepository.create(
            owner.id,
            title,
            description,
            isActive,
            targetCommits.map {
                commitService.createIfNotExist(it.owner, it.repository, it.sha)
            },
        )
    }
}
