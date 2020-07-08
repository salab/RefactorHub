package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
fun Routing.api() {

    route("/api") {
        route("/users") {
            val userController: UserController by inject()
            get("/{id}") {
                val id = call.parameters.getOrFail<Int>("id")
                call.respond(transaction { userController.get(id) })
            }
        }
    }
}
