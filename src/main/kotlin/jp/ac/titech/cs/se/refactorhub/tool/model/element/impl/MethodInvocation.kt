package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementInMethod
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

data class MethodInvocation(
    override val methodName: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : CodeElementInMethod {
    override val type: CodeElementType get() = CodeElementType.MethodInvocation
}
