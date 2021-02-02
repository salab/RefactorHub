package jp.ac.titech.cs.se.refactorhub.core.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementInMethod
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location

@JsonDeserialize(`as` = ParameterType::class)
data class ParameterType(
    override val representation: String? = null,
    override val methodName: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : Type, CodeElementInMethod {
    override val type: CodeElementType get() = CodeElementType.ParameterType
}
