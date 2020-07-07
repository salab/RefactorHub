package jp.ac.titech.cs.se.refactorhub.old.models.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import jp.ac.titech.cs.se.refactorhub.old.models.element.ElementInClass
import jp.ac.titech.cs.se.refactorhub.old.models.element.data.Location

@JsonDeserialize(`as` = FieldDeclaration::class)
data class FieldDeclaration(
    val name: String = "",
    override val className: String = "",
    override val location: Location = Location()
) : ElementInClass {
    override val type: Element.Type get() = Element.Type.FieldDeclaration
}
