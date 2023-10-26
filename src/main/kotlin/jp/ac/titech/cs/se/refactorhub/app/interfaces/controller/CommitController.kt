package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CommitService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class CommitController : KoinComponent {
    private val commitService: CommitService by inject()

    fun getDetail(owner: String, repository: String, sha: String): CommitDetail {
        return commitService.getDetail(owner, repository, sha)
    }
}
