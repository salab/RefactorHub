package jp.ac.titech.cs.se.refactorhub.tool.model.element

import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location

interface CodeElement {
    val type: CodeElementType
    val location: Location?
}

interface CodeElementInClass : CodeElement {
    val className: String?
}

interface CodeElementInMethod : CodeElementInClass {
    val methodName: String?
}
