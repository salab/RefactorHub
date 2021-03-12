package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.refactoring.RefactoringType

data class RefactoringType(
    val id: Int,
    val ownerId: Int,
    override val name: String,
    override val before: Map<String, CodeElementMetadata> = mapOf(),
    override val after: Map<String, CodeElementMetadata> = mapOf(),
    override val description: String = "",
    override val url: String = ""
) : RefactoringType
