package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CodeElementController
import org.koin.ktor.ext.inject

@Location("")
class GetCodeElementTypes

fun Route.codeElementTypes() {
    route("/code_element_types") {
        val codeElementController: CodeElementController by inject()
        get<GetCodeElementTypes> {
            call.respond(codeElementController.getTypes())
        }
    }
}
