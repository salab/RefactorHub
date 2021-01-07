package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.EditorController
import jp.ac.titech.cs.se.refactorhub.tool.model.DiffCategory
import org.koin.ktor.ext.inject

@KtorExperimentalLocationsAPI
@Location("/content")
data class GetFileContent(
    val sha: String,
    val owner: String,
    val repository: String,
    val category: DiffCategory,
    val path: String
)

@KtorExperimentalLocationsAPI
fun Route.editor() {
    route("/editor") {
        val editorController: EditorController by inject()
        get<GetFileContent> {
            call.respond(
                editorController.getFileContent(
                    it.sha,
                    it.owner,
                    it.repository,
                    it.category,
                    it.path
                )
            )
        }
    }
}
