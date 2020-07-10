package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ElementController
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/types")
class GetElementTypes

@KtorExperimentalLocationsAPI
fun Route.elements() {
    val elementController: ElementController by inject()
    get<GetElementTypes> {
        call.respond(elementController.getTypes())
    }
}
