package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Commit

interface CommitRepository {
    fun findBySha(sha: String): Commit?
    fun save(commit: Commit): Commit
}
