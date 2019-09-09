package jp.ac.titech.cs.se.refactorhub.controllers.editor

import jp.ac.titech.cs.se.refactorhub.services.EditorService
import jp.ac.titech.cs.se.refactorhub.services.GitHubService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/editor")
class EditorController(
    private val gitHubService: GitHubService,
    private val editorService: EditorService
) {

    @GetMapping("/text_model")
    fun getTextModel(
        @RequestParam sha: String,
        @RequestParam owner: String,
        @RequestParam repository: String,
        @RequestParam path: String
    ) =
        ResponseEntity.ok(editorService.createTextModel(gitHubService.getContent(sha, owner, repository, path)))

}
