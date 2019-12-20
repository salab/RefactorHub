package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.AddElementRequest
import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateElementRequest
import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateRequest
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/draft")
class DraftController(
    private val refactoringService: RefactoringService,
    private val draftService: DraftService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = draftService.getByOwner(id)

    @PostMapping("/{id}/save")
    fun save(@PathVariable("id") id: Long) = refactoringService.save(draftService.getByOwner(id))

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable("id") id: Long) = draftService.cancel(id)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody request: UpdateRequest?
    ) = draftService.update(id, request?.description, request?.type)

    @PatchMapping("/{id}/before/{key}")
    fun updateBeforeElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @RequestBody request: UpdateElementRequest
    ) = draftService.updateBeforeElement(id, key, request.element)

    @PatchMapping("/{id}/after/{key}")
    fun updateAfterElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @RequestBody request: UpdateElementRequest
    ) = draftService.updateAfterElement(id, key, request.element)

    @PutMapping("/{id}/before")
    fun addBeforeElement(
        @PathVariable("id") id: Long,
        @RequestBody request: AddElementRequest
    ) = draftService.addBeforeElement(id, request.key, request.type)

    @PutMapping("/{id}/after")
    fun addAfterElement(
        @PathVariable("id") id: Long,
        @RequestBody request: AddElementRequest
    ) = draftService.addAfterElement(id, request.key, request.type)

}
