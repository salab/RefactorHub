package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import io.ktor.features.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata
import org.koin.core.KoinComponent
import org.koin.core.inject

class RefactoringTypeService : KoinComponent {
    private val refactoringTypeRepository: RefactoringTypeRepository by inject()

    fun create(
        name: String,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String
    ): RefactoringType {
        return refactoringTypeRepository.create(name, before, after, description)
    }

    fun getAll(): List<RefactoringType> {
        return refactoringTypeRepository.findAll()
    }

    fun get(id: Int): RefactoringType {
        val type = refactoringTypeRepository.findById(id)
        type ?: throw NotFoundException("RefactoringType(id=$id) is not found")
        return type
    }

    fun getByName(name: String): RefactoringType {
        val type = refactoringTypeRepository.findByName(name)
        type ?: throw NotFoundException("RefactoringType(name=$name) is not found")
        return type
    }
}