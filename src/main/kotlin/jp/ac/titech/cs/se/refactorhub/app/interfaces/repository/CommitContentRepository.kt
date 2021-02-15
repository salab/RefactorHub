package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent

interface CommitContentRepository {
    fun find(owner: String, repository: String, sha: String): CommitContent?
    fun save(content: CommitContent): CommitContent
}
