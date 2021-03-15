package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring

interface ExperimentRepository {
    fun create(
        title: String,
        description: String,
        refactorings: List<Refactoring>,
        userId: Int
    ): Experiment

    fun findAll(): List<Experiment>
    fun findById(id: Int): Experiment?
}
