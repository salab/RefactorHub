package jp.ac.titech.cs.se.refactorhub.models.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.models.element.ElementInClass
import jp.ac.titech.cs.se.refactorhub.models.element.data.Location

@JsonDeserialize(`as` = ConstructorDeclaration::class)
data class ConstructorDeclaration(
    val name: String = "",
    override val className: String = "",
    override val location: Location = Location()
) : ElementInClass {
    override val type: Element.Type get() = Element.Type.ConstructorDeclaration
}
