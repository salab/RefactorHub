package jp.ac.titech.cs.se.refactorhub.old.controllers.refactoring

import jp.ac.titech.cs.se.refactorhub.old.controllers.refactoring.requests.AddRefactoringRequest
import jp.ac.titech.cs.se.refactorhub.old.controllers.refactoring.requests.AddRefactoringTypeRequest
import jp.ac.titech.cs.se.refactorhub.old.services.DraftService
import jp.ac.titech.cs.se.refactorhub.old.services.RefactoringService
import jp.ac.titech.cs.se.refactorhub.old.services.RefactoringTypeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/refactoring")
class RefactoringController(
    private val refactoringService: RefactoringService,
    private val refactoringTypeService: RefactoringTypeService,
    private val draftService: DraftService
) {

    @PutMapping
    fun add(@RequestBody request: AddRefactoringRequest) =
        refactoringService.add(request.type, request.description, request.commit, request.data)

    @GetMapping
    fun getAll() = refactoringService.getAll()

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = refactoringService.get(id)

    @GetMapping("/{id}/children")
    fun getChildren(@PathVariable("id") id: Long) = refactoringService.getChildren(id)

    @GetMapping("/{id}/drafts")
    fun getDrafts(@PathVariable("id") id: Long) = refactoringService.getDrafts(id)

    @PostMapping("/{id}/fork")
    fun fork(@PathVariable("id") id: Long) = draftService.fork(refactoringService.get(id))

    @PostMapping("/{id}/edit")
    fun edit(@PathVariable("id") id: Long) = draftService.edit(refactoringService.get(id))

    @GetMapping("/types")
    fun types() = refactoringTypeService.getAll()

    @PutMapping("/types")
    fun addType(@RequestBody request: AddRefactoringTypeRequest) =
        refactoringTypeService.create(request.name, request.before, request.after)
}
