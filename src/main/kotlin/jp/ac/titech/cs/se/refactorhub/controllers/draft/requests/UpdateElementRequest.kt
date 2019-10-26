package jp.ac.titech.cs.se.refactorhub.controllers.draft.requests

import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class UpdateElementRequest(
    val element: Element
)