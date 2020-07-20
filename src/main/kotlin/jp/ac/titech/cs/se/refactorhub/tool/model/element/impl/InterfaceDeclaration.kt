package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

@JsonDeserialize(`as` = InterfaceDeclaration::class)
data class InterfaceDeclaration(
    val name: String? = null,
    override val location: Location? = null
) : CodeElement {
    override val type: CodeElementType get() = CodeElementType.InterfaceDeclaration
}
