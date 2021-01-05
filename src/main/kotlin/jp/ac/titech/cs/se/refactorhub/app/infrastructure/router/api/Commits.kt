package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CommitController
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/{sha}")
data class GetCommit(val sha: String)

@KtorExperimentalLocationsAPI
@Location("/{sha}/detail")
data class GetCommitDetail(val sha: String)

@KtorExperimentalLocationsAPI
fun Route.commits() {
    route("/commits") {
        val commitController: CommitController by inject()
        get<GetCommit> {
            call.respond(commitController.get(it.sha))
        }
        get<GetCommitDetail> {
            call.respond(commitController.getDetail(it.sha))
        }
    }
}
