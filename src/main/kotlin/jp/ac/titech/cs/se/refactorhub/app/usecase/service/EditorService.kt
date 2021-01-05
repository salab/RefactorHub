package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.tool.model.editor.FileContent

class EditorService {

    fun getFileContent(
        sha: String,
        owner: String,
        repository: String,
        path: String
    ): FileContent {
        return jp.ac.titech.cs.se.refactorhub.tool.editor.getFileContent(sha, owner, repository, path)
    }
}
