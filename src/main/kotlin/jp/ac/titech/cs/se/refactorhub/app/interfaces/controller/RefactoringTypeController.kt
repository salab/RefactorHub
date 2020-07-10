package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.RefactoringType
import org.koin.core.KoinComponent

class RefactoringTypeController : KoinComponent {

    data class CreateRefactoringTypeBody(
        val name: String,
        val before: Map<String, CodeElementHolder>,
        val after: Map<String, CodeElementHolder>
    )

    fun create(body: CreateRefactoringTypeBody): RefactoringType {
        TODO()
    }

    fun getAll(): List<RefactoringType> {
        TODO()
    }
}
