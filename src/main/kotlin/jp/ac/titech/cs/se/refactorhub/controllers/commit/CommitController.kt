package jp.ac.titech.cs.se.refactorhub.controllers.commit

import jp.ac.titech.cs.se.refactorhub.services.CommitService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/commit/{sha}")
class CommitController(
    private val commitService: CommitService
) {

    @GetMapping
    fun get(@PathVariable("sha") sha: String) = ResponseEntity.ok(commitService.get(sha))

    @GetMapping("/info")
    fun getInfo(@PathVariable("sha") sha: String) = ResponseEntity.ok(commitService.getInfo(sha))
}
