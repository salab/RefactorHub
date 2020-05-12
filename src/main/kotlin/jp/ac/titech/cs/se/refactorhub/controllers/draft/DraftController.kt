package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.AddElementKeyRequest
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

    @PatchMapping("/{id}/before/{key}/{index}")
    fun updateBeforeElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @PathVariable("index") index: Int,
        @RequestBody request: UpdateElementRequest
    ) = draftService.updateBeforeElement(id, key, index, request.element)

    @PatchMapping("/{id}/after/{key}/{index}")
    fun updateAfterElement(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String,
        @PathVariable("index") index: Int,
        @RequestBody request: UpdateElementRequest
    ) = draftService.updateAfterElement(id, key, index, request.element)

    @PutMapping("/{id}/before/{key}")
    fun addBeforeElementLocation(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String
    ) = draftService.addBeforeElementLocation(id, key)

    @PutMapping("/{id}/after/{key}")
    fun addAfterElementLocation(
        @PathVariable("id") id: Long,
        @PathVariable("key") key: String
    ) = draftService.addAfterElementLocation(id, key)

    @PutMapping("/{id}/before")
    fun addBeforeElementKey(
        @PathVariable("id") id: Long,
        @RequestBody request: AddElementKeyRequest
    ) = draftService.addBeforeElementKey(id, request.key, request.type, request.multiple)

    @PutMapping("/{id}/after")
    fun addAfterElementKey(
        @PathVariable("id") id: Long,
        @RequestBody request: AddElementKeyRequest
    ) = draftService.addAfterElementKey(id, request.key, request.type, request.multiple)

}
