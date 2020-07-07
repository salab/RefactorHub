package jp.ac.titech.cs.se.refactorhub.old.controllers.refactoring.requests

import jp.ac.titech.cs.se.refactorhub.old.models.element.Element

data class AddRefactoringTypeRequest(
    val name: String,
    val before: Map<String, Element.Info>,
    val after: Map<String, Element.Info>
)

