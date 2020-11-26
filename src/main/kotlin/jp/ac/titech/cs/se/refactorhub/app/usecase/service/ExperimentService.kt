package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ExperimentRepository
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.koin.core.KoinComponent
import org.koin.core.inject

class ExperimentService : KoinComponent {
    private val experimentRepository: ExperimentRepository by inject()
    private val refactoringRepository: RefactoringRepository by inject()

    fun getAll(): List<Experiment> {
        return experimentRepository.findAll()
    }

    fun get(id: Int): Experiment {
        val experiment = experimentRepository.findById(id)
        experiment ?: throw NotFoundException("Experiment(id=$id) is not found")
        return experiment
    }

    fun getRefactorings(id: Int): List<Refactoring> {
        val experiment = get(id)
        return refactoringRepository.findByExperimentId(experiment.id)
    }
}
