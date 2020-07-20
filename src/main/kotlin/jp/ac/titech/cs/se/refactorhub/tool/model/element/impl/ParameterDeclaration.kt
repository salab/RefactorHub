package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementInMethod
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

@JsonDeserialize(`as` = ParameterDeclaration::class)
data class ParameterDeclaration(
    val name: String? = null,
    override val methodName: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : CodeElementInMethod {
    override val type: CodeElementType get() = CodeElementType.ParameterDeclaration
}
