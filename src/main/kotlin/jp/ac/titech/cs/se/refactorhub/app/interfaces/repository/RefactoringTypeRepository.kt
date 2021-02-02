package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata

interface RefactoringTypeRepository {
    fun findAll(): List<RefactoringType>
    fun findById(id: Int): RefactoringType?
    fun findByName(name: String): RefactoringType?
    fun deleteById(id: Int)
    fun create(
        name: String,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String
    ): RefactoringType
}
