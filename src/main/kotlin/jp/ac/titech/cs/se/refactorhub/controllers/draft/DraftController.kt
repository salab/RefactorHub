package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateElementRequest
import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateRequest
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/draft")
class DraftController(
    private val refactoringService: RefactoringService,
    private val draftService: DraftService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = ResponseEntity.ok(draftService.getByOwner(id))

    @PostMapping("/{id}/save")
    fun save(@PathVariable("id") id: Long): ResponseEntity<Refactoring> =
        ResponseEntity.ok(refactoringService.save(draftService.getByOwner(id)))

    @PatchMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody request: UpdateRequest?
    ) = ResponseEntity.ok(draftService.update(id, request?.description, request?.type))

    @PatchMapping("/{id}/before/{key}")
    fun updateBeforeElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @RequestBody request: UpdateElementRequest
    ) = ResponseEntity.ok(draftService.updateBeforeElement(id, key, request.element))

    @PatchMapping("/{id}/after/{key}")
    fun updateAfterElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @RequestBody request: UpdateElementRequest
    ) = ResponseEntity.ok(draftService.updateAfterElement(id, key, request.element))

}
