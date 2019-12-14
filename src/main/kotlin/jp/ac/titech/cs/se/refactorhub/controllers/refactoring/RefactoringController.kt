package jp.ac.titech.cs.se.refactorhub.controllers.refactoring

import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/refactoring")
class RefactoringController(
    private val refactoringService: RefactoringService,
    private val refactoringTypeService: RefactoringTypeService,
    private val draftService: DraftService,
    private val userService: UserService
) {

    @GetMapping
    fun getAll() = refactoringService.getAll()

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = refactoringService.get(id)

    @GetMapping("/{id}/children")
    fun getChildren(@PathVariable("id") id: Long) = refactoringService.getChildren(id)

    @GetMapping("/{id}/drafts")
    fun getDrafts(@PathVariable("id") id: Long) = refactoringService.getDrafts(id)

    @PostMapping("/{id}/fork")
    fun fork(@PathVariable("id") id: Long) = draftService.fork(userService.me(), refactoringService.get(id))

    @GetMapping("/types")
    fun types() = refactoringTypeService.getAll()

}
