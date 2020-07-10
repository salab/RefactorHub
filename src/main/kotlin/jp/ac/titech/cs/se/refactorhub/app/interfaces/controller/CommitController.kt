package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.CommitDetail
import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import org.koin.core.KoinComponent

class CommitController : KoinComponent {
    fun get(sha: String): Commit {
        TODO()
    }

    fun getDetail(sha: String): CommitDetail {
        TODO()
    }
}
