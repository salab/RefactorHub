package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.login
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.*

fun Routing.root() {
    login()

    route("/api") {
        users()
        annotations()
        changeTypes()
        codeElementTypes()
        experiments()
        actions()
    }
}
