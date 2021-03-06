package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.login
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.actions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.annotator
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.drafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.elements
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.experiments
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.refactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api.users
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Routing.root() {
    login()

    route("/api") {
        users()
        commits()
        refactorings()
        refactoringTypes()
        drafts()
        annotator()
        elements()
        experiments()
        actions()
    }
}
