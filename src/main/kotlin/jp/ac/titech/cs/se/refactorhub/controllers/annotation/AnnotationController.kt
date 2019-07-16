package jp.ac.titech.cs.se.refactorhub.controllers.annotation

import jp.ac.titech.cs.se.refactorhub.services.AnnotationService
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/annotation")
class AnnotationController(
    private val annotationService: AnnotationService,
    private val draftService: DraftService,
    private val userService: UserService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = ResponseEntity.ok(annotationService.get(id))

    @GetMapping("/{id}/children")
    fun getChildren(@PathVariable("id") id: Long) = ResponseEntity.ok(annotationService.getChildren(id))

    @GetMapping("/{id}/drafts")
    fun getDrafts(@PathVariable("id") id: Long) = ResponseEntity.ok(annotationService.getDrafts(id))

    @PostMapping("/{id}/fork")
    fun fork(@PathVariable("id") id: Long) =
        ResponseEntity.ok(draftService.fork(userService.me(), annotationService.get(id)))

}
