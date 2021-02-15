package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CodeElementController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/types")
class GetElementTypes

@KtorExperimentalLocationsAPI
@Location("/schemas")
class GetElementSchemas

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.elements() {
    route("/elements") {
        val codeElementController: CodeElementController by inject()
        get<GetElementTypes> {
            call.respond(codeElementController.getTypes())
        }
        get<GetElementSchemas> {
            call.respond(codeElementController.getSchemas())
        }
    }
}
