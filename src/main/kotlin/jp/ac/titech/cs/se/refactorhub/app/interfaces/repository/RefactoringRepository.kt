package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring

interface RefactoringRepository {
    fun findAll(): List<Refactoring>
    fun findById(id: Int): Refactoring?
    fun findByOwnerId(ownerId: Int): List<Refactoring>
    fun findByParentId(id: Int): List<Refactoring>
    fun findByExperimentId(experimentId: Int): List<Refactoring>

    fun create(
        commit: Commit,
        typeName: String,
        data: Refactoring.Data,
        description: String,
        userId: Int,
        parentId: Int?,
        isVerified: Boolean = false
    ): Refactoring

    fun updateById(
        id: Int,
        typeName: String? = null,
        data: Refactoring.Data? = null,
        description: String? = null
    ): Refactoring

    fun deleteById(id: Int)
}
