package jp.ac.titech.cs.se.refactorhub.controllers.draft

import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.services.AnnotationService
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/draft")
class DraftController(
    private val annotationService: AnnotationService,
    private val draftService: DraftService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = ResponseEntity.ok(draftService.getByOwner(id))

    @PostMapping("/{id}/save")
    fun save(@PathVariable("id") id: Long): ResponseEntity<Annotation> =
        ResponseEntity.ok(annotationService.save(draftService.getByOwner(id)))

    @PatchMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestParam description: String?
    ): ResponseEntity<Draft> {
        val draft = draftService.getByOwner(id)
        if (description != null) draft.description = description
        return ResponseEntity.ok(draftService.save(draft))
    }

}
