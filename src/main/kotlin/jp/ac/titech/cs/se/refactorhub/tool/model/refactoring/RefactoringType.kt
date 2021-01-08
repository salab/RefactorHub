package jp.ac.titech.cs.se.refactorhub.tool.model.refactoring

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata

interface RefactoringType {
    val name: String
    val description: String
    val url: String
    val before: Map<String, CodeElementMetadata>
    val after: Map<String, CodeElementMetadata>
}
