package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.core.model.editor.CommitFileContents

interface FileContentRepository {
    fun find(sha: String): CommitFileContents?
    fun save(contents: CommitFileContents): CommitFileContents
}
