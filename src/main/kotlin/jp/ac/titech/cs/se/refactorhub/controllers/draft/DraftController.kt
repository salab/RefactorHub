package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.AddElementKeyRequest
import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateElementRequest
import jp.ac.titech.cs.se.refactorhub.controllers.draft.requests.UpdateRequest
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @PostMapping("/{id}/discard")
    fun discard(@PathVariable("id") id: Long) = draftService.discard(id)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody request: UpdateRequest?
    ) = draftService.update(id, request?.description, request?.type)

    @PatchMapping("/{id}/{category}/{key}/{index}")
    fun updateElement(
        @PathVariable("id") id: Long,
        @PathVariable("category") category: String,
        @PathVariable("key") key: String,
        @PathVariable("index") index: Int,
        @RequestBody request: UpdateElementRequest
    ) = draftService.updateElement(id, category, key, index, request.element)

    @DeleteMapping("/{id}/{category}/{key}/{index}")
    fun deleteElement(
        @PathVariable("id") id: Long,
        @PathVariable("category") category: String,
        @PathVariable("key") key: String,
        @PathVariable("index") index: Int
    ) = draftService.deleteElement(id, category, key, index)

    @PutMapping("/{id}/{category}/{key}")
    fun addElement(
        @PathVariable("id") id: Long,
        @PathVariable("category") category: String,
        @PathVariable("key") key: String
    ) = draftService.addElement(id, category, key)

    @PutMapping("/{id}/{category}")
    fun addElementKey(
        @PathVariable("id") id: Long,
        @PathVariable("category") category: String,
        @RequestBody request: AddElementKeyRequest
    ) = draftService.addElementKey(id, category, request.key, request.type, request.multiple)

    @DeleteMapping("/{id}/{category}/{key}")
    fun deleteElementKey(
        @PathVariable("id") id: Long,
        @PathVariable("category") category: String,
        @PathVariable("key") key: String
    ) = draftService.deleteElementKey(id, category, key)
}
