package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

@JsonDeserialize(`as` = EnumDeclaration::class)
data class EnumDeclaration(
    override val name: String? = null,
    override val location: Location? = null
) : TypeDeclaration {
    override val type: CodeElementType get() = CodeElementType.EnumDeclaration
}
