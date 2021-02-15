package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CommitController
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/{owner}/{repository}/{sha}")
data class GetCommit(val owner: String, val repository: String, val sha: String)

@KtorExperimentalLocationsAPI
@Location("/{owner}/{repository}/{sha}/detail")
data class GetCommitDetail(val owner: String, val repository: String, val sha: String)

@KoinApiExtension
@KtorExperimentalLocationsAPI
fun Route.commits() {
    route("/commits") {
        val commitController: CommitController by inject()
        get<GetCommit> {
            call.respond(commitController.get(it.owner, it.repository, it.sha))
        }
        get<GetCommitDetail> {
            call.respond(commitController.getDetail(it.owner, it.repository, it.sha))
        }
    }
}
