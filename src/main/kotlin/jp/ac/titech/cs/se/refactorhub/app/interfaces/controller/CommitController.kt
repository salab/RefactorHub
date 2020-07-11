package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CommitService
import org.koin.core.KoinComponent
import org.koin.core.inject

class CommitController : KoinComponent {
    private val commitService: CommitService by inject()

    fun get(sha: String): Commit {
        return commitService.get(sha)
    }

    fun getDetail(sha: String, token: String?): CommitDetail {
        return commitService.getDetail(sha, token)
    }
}
