package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
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
