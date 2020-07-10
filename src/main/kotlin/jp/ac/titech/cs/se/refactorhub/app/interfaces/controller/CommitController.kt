package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/commits")
class CommitController : KoinComponent {

    @Location("/{sha}")
    data class GetCommit(val sha: String)

    @Location("/{sha}/detail")
    data class GetCommitDetail(val sha: String)

    fun get(params: GetCommit): Commit {
        TODO()
    }

    fun getDetail(params: GetCommitDetail): CommitDetail {
        TODO()
    }
}
