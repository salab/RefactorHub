package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent

interface CommitContentRepository {
    fun find(sha: String): CommitContent?
    fun save(content: CommitContent): CommitContent
}
