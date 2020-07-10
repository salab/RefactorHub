package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/elements")
class ElementController : KoinComponent {

    @Location("/types")
    class GetElementTypes

    fun getTypes(): List<CodeElement> {
        TODO()
    }
}
