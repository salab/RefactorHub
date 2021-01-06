package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents

interface FileContentRepository {
    fun find(owner: String, repository: String, sha: String): CommitFileContents?
    fun save(contents: CommitFileContents): CommitFileContents
}
