package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementInClass
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

data class ConstructorDeclaration(
    val name: String,
    override val className: String,
    override val location: Location
) : CodeElementInClass {
    override val type: CodeElementType get() = CodeElementType.ConstructorDeclaration
}
