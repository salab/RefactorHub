package jp.ac.titech.cs.se.refactorhub.old.controllers.element

import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/element")
class ElementController {

    @GetMapping("/types")
    fun types() = Element.Type.values().map { it.name }
}
