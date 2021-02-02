package jp.ac.titech.cs.se.refactorhub.core.model.element.impl

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

interface TypeDeclaration : CodeElement {
    val name: String?
}
