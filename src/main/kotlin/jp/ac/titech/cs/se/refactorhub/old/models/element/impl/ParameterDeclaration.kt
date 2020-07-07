package jp.ac.titech.cs.se.refactorhub.old.models.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import jp.ac.titech.cs.se.refactorhub.old.models.element.ElementInMethod
import jp.ac.titech.cs.se.refactorhub.old.models.element.data.Location

@JsonDeserialize(`as` = ParameterDeclaration::class)
data class ParameterDeclaration(
    val name: String = "",
    override val methodName: String = "",
    override val className: String = "",
    override val location: Location = Location()
) : ElementInMethod {
    override val type: Element.Type get() = Element.Type.ParameterDeclaration
}
