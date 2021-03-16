package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.model.CreateRefactoringBody
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ExperimentService : KoinComponent {
    private val experimentRepository: ExperimentRepository by inject()
    private val refactoringRepository: RefactoringRepository by inject()
    private val userService: UserService by inject()
    private val refactoringService: RefactoringService by inject()

    fun create(
        title: String,
        description: String,
        refactorings: List<CreateRefactoringBody>,
        isActive: Boolean,
        userId: Int?
    ): Experiment {
        val user = userService.getMe(userId)
        return experimentRepository.create(
            title,
            description,
            refactorings.map {
                refactoringService.create(it.commit, it.type, it.data, it.description, user.id)
            },
            isActive,
            user.id
        )
    }

    fun getAll(): List<Experiment> {
        return experimentRepository.findAll()
    }

    fun get(id: Int): Experiment {
        val experiment = experimentRepository.findById(id)
        experiment ?: throw NotFoundException("Experiment(id=$id) is not found")
        return experiment
    }

    fun getResult(id: Int): List<Refactoring> {
        val refactorings = getRefactorings(id)
        return refactorings.map {
            refactoringRepository.findByParentId(it.id)
        }.flatten()
    }

    fun getRefactorings(id: Int): List<Refactoring> {
        val experiment = get(id)
        return refactoringRepository.findByExperimentId(experiment.id).sortedBy { it.id }
    }
}
