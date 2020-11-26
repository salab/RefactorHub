package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Experiment

interface ExperimentRepository {
    fun findAll(): List<Experiment>
    fun findById(id: Int): Experiment?
}
