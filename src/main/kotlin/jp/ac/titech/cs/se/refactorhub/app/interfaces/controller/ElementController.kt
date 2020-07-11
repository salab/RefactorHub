package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ElementService
import org.koin.core.KoinComponent
import org.koin.core.inject

@KtorExperimentalLocationsAPI
@Location("/elements")
class ElementController : KoinComponent {
    private val elementService: ElementService by inject()

    fun getTypes(): List<String> {
        return elementService.getTypes()
    }
}
