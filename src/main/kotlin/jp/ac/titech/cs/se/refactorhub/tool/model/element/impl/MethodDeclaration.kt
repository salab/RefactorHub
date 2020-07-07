package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementInClass
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

data class MethodDeclaration(
    val name: String? = null,
    override val className: String? = null,
    override val location: Location? = null
) : CodeElementInClass {
    override val type: CodeElementType get() = CodeElementType.MethodDeclaration
}
