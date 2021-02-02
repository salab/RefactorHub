package jp.ac.titech.cs.se.refactorhub.core.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementInClass
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location

@JsonDeserialize(`as` = ReturnType::class)
data class ReturnType(
    override val representation: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : Type, CodeElementInClass {
    override val type: CodeElementType get() = CodeElementType.ReturnType
}
