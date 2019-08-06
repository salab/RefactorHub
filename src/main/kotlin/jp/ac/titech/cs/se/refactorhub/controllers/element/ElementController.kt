package jp.ac.titech.cs.se.refactorhub.controllers.element

import jp.ac.titech.cs.se.refactorhub.models.element.Element
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/element")
class ElementController {

    @GetMapping("/types")
    fun types() = ResponseEntity.ok(Element.Type.values().map { it.name })

}