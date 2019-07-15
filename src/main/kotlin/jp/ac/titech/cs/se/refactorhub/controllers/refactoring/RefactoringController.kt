package jp.ac.titech.cs.se.refactorhub.controllers.refactoring

import jp.ac.titech.cs.se.refactorhub.models.refactoring.Refactoring
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/refactoring")
class RefactoringController {

    @GetMapping("/types")
    fun types() = ResponseEntity.ok(Refactoring.Type.values().map { it.name })

}
