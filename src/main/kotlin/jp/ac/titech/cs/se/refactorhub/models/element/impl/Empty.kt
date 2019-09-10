package jp.ac.titech.cs.se.refactorhub.models.element.impl

import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.models.element.data.Location

class Empty : Element {
    override val location: Location = Location()
    override val type: Element.Type get() = Element.Type.Empty
}