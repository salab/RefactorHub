package jp.ac.titech.cs.se.refactorhub.old.services

import jp.ac.titech.cs.se.refactorhub.old.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.old.models.RefactoringType
import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import jp.ac.titech.cs.se.refactorhub.old.repositories.RefactoringTypeRepository
import org.springframework.stereotype.Service

@Service
class RefactoringTypeService(
    private val refactoringTypeRepository: RefactoringTypeRepository
) {

    fun getAll(): List<RefactoringType> = refactoringTypeRepository.findAll()

    fun get(id: Long): RefactoringType {
        val optional = refactoringTypeRepository.findById(id)
        if (optional.isPresent) return optional.get()
        throw NotFoundException("RefactoringType(id=$id) is not found.")
    }

    fun getByName(name: String): RefactoringType {
        val optional = refactoringTypeRepository.findByName(name)
        if (optional.isPresent) return optional.get()
        throw NotFoundException("RefactoringType(name=$name) is not found.")
    }

    fun create(
        name: String,
        before: Map<String, Element.Info> = mapOf(),
        after: Map<String, Element.Info> = mapOf()
    ): RefactoringType {
        val type = refactoringTypeRepository.findByName(name)
        return if (type.isPresent) {
            type.get()
        } else {
            refactoringTypeRepository.save(RefactoringType(name, before, after))
        }
    }

    fun save(type: RefactoringType): RefactoringType = refactoringTypeRepository.save(type)

    fun delete(id: Long) = refactoringTypeRepository.deleteById(id)
}
