package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.ChangeType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import java.util.UUID

interface ChangeTypeRepository {
    fun findByName(name: String): ChangeType?
    fun findAll(): List<ChangeType>

    fun create(
        name: String,
        ownerId: UUID,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String,
        referenceUrl: String,
    ): ChangeType

    fun deleteByName(name: String)
}
