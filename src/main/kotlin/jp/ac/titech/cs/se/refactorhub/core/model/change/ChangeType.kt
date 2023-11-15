package jp.ac.titech.cs.se.refactorhub.core.model.change

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata

interface ChangeType {
    val name: String
    val description: String
    val referenceUrl: String
    val before: Map<String, CodeElementMetadata>
    val after: Map<String, CodeElementMetadata>
}
