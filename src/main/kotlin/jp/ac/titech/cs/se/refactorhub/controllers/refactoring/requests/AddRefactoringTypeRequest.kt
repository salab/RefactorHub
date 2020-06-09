package jp.ac.titech.cs.se.refactorhub.controllers.refactoring.requests

import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class AddRefactoringTypeRequest(
    val name: String = "",
    val before: Map<String, Element.Info> = mapOf(),
    val after: Map<String, Element.Info> = mapOf()
)

