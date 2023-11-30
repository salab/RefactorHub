package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.login
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.*
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@KtorExperimentalLocationsAPI
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
