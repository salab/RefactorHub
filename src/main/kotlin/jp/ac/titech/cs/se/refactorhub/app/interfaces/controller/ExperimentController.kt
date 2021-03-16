package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.CreateRefactoringBody
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ExperimentService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ExperimentController : KoinComponent {
    private val experimentService: ExperimentService by inject()

    data class CreateExperimentBody(
        val title: String,
        val description: String,
        val refactorings: List<CreateRefactoringBody>
    )

    fun create(body: CreateExperimentBody, userId: Int?): Experiment {
        return experimentService.create(body.title, body.description, body.refactorings, true, userId)
    }

    fun getAll(): List<Experiment> {
        return experimentService.getAll()
    }

    fun get(id: Int): Experiment {
        return experimentService.get(id)
    }

    fun getRefactorings(id: Int): List<Refactoring> {
        return experimentService.getRefactorings(id)
    }
}
