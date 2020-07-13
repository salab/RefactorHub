package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata
import org.koin.core.KoinComponent
import org.koin.core.inject

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
}
