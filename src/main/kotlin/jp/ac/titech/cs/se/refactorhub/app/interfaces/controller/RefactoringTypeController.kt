package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringTypeController : KoinComponent {
    private val refactoringTypeService: RefactoringTypeService by inject()

    data class CreateRefactoringTypeBody(
        val name: String,
        val before: Map<String, CodeElementMetadata>,
        val after: Map<String, CodeElementMetadata>,
        val description: String = ""
    )

    fun create(body: CreateRefactoringTypeBody): RefactoringType {
        return refactoringTypeService.create(body.name, body.before, body.after, body.description)
    }

    fun getAll(): List<RefactoringType> {
        return refactoringTypeService.getAll()
    }

    fun get(name: String): RefactoringType {
        return refactoringTypeService.getByName(name)
    }
}
