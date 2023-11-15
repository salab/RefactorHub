package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import java.util.UUID

interface ExperimentRepository {
    fun findById(id: UUID): Experiment?
    fun findAll(): List<Experiment>

    fun create(
        ownerId: UUID,
        title: String,
        description: String,
        isActive: Boolean,
        targetCommits: List<Commit>,
    ): Experiment
}
