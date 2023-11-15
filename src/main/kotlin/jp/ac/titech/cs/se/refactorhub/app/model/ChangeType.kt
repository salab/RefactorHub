package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.change.ChangeType
import java.util.UUID

data class ChangeType(
    override val name: String,
    val ownerId: UUID,
    override val before: Map<String, CodeElementMetadata> = mapOf(),
    override val after: Map<String, CodeElementMetadata> = mapOf(),
    override val description: String = "",
    override val referenceUrl: String = "",
) : ChangeType
