package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringTypeService : KoinComponent {
    private val refactoringTypeRepository: RefactoringTypeRepository by inject()
    private val userService: UserService by inject()

    fun create(
        name: String,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String,
        userId: Int?
    ): RefactoringType {
        val user = userService.getMe(userId)
        val type = refactoringTypeRepository.findByName(name)
        if (type != null) throw BadRequestException("RefactoringType(name=$name) already exists")
        return refactoringTypeRepository.create(name, before, after, description, user.id)
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
