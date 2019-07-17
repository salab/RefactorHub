package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.services.AnnotationService
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/draft")
class DraftController(
    private val annotationService: AnnotationService,
    private val draftService: DraftService,
    private val userService: UserService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = ResponseEntity.ok(draftService.get(id))

    @PostMapping("/{id}/save")
    fun save(@PathVariable("id") id: Long): ResponseEntity<Annotation> =
        ResponseEntity.ok(annotationService.save(draftService.getByOwner(id, userService.me())))

}
