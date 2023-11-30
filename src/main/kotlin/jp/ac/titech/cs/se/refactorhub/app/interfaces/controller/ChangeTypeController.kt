package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.ChangeType
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ChangeTypeService
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
class ChangeTypeController : KoinComponent {
    private val changeTypeService: ChangeTypeService by inject()

    fun getAll(): List<ChangeType> {
        return changeTypeService.getAll()
    }

    fun create(
        changeTypeName: String,
        ownerId: UUID?,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String,
        referenceUrl: String = ""
    ): ChangeType {
        return changeTypeService.create(changeTypeName, ownerId, before, after, description, referenceUrl)
    }
}
