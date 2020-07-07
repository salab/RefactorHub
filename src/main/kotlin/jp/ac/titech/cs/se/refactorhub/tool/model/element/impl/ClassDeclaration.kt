package jp.ac.titech.cs.se.refactorhub.tool.model.element.impl

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

data class ClassDeclaration(
    val name: String? = null,
    override val location: Location? = null
) : CodeElement {
    override val type: CodeElementType get() = CodeElementType.ClassDeclaration
}
