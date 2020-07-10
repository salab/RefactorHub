package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.RefactoringType
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/refactoring_types")
class RefactoringTypeController : KoinComponent {

    @Location("")
    class CreateRefactoringType
    data class CreateRefactoringTypeBody(
        val name: String,
        val before: Map<String, CodeElementHolder>,
        val after: Map<String, CodeElementHolder>
    )

    @Location("")
    class GetAllRefactoringTypes

    fun create(params: CreateRefactoringType, body: CreateRefactoringTypeBody): RefactoringType {
        TODO()
    }

    fun getAll(): List<RefactoringType> {
        TODO()
    }
}
