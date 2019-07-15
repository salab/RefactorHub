package jp.ac.titech.cs.se.refactorhub.models.refactoring.impl

import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.models.element.ElementSet
import jp.ac.titech.cs.se.refactorhub.models.refactoring.Refactoring

data class Custom(
    override val before: Before = Before(),
    override val after: After = After()
) : Refactoring {
    override val type: Refactoring.Type get() = Refactoring.Type.Custom

    data class Before(
        override val custom: MutableMap<String, Element> = mutableMapOf()
    ) : ElementSet

    data class After(
        override val custom: MutableMap<String, Element> = mutableMapOf()
    ) : ElementSet
}
