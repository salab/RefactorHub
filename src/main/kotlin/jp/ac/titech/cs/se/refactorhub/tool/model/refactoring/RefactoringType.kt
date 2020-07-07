package jp.ac.titech.cs.se.refactorhub.tool.model.refactoring

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata

data class RefactoringType(
    val name: String,
    val before: Map<String, CodeElementMetadata> = mapOf(),
    val after: Map<String, CodeElementMetadata> = mapOf(),
    val description: String = ""
)
