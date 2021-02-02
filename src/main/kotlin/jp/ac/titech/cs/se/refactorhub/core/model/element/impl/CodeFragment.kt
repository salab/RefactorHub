package jp.ac.titech.cs.se.refactorhub.core.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementInMethod
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location

@JsonDeserialize(`as` = CodeFragment::class)
data class CodeFragment(
    override val methodName: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : CodeElementInMethod {
    override val type: CodeElementType get() = CodeElementType.CodeFragment
}
