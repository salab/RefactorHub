package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.exception.UnauthorizedException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.ChangeTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.model.ChangeType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class ChangeTypeService : KoinComponent {
    private val changeTypeRepository: ChangeTypeRepository by inject()

    fun get(changeTypeName: String): ChangeType {
        val changeType = changeTypeRepository.findByName(changeTypeName)
        return changeType ?: throw NotFoundException("ChangeType(name=$changeTypeName) is not found")
    }
    fun getAll(): List<ChangeType> {
        return changeTypeRepository.findAll()
    }

    fun create(
        changeTypeName: String,
        ownerId: UUID?,
        before: Map<String, CodeElementMetadata>,
        after: Map<String, CodeElementMetadata>,
        description: String,
        referenceUrl: String = "",
    ): ChangeType {
        if (ownerId == null) throw UnauthorizedException("You are not logged in")
        val changeType = changeTypeRepository.findByName(changeTypeName)
        if (changeType != null) throw BadRequestException("ChangeType(name=$changeTypeName) already exists")
        return changeTypeRepository.create(changeTypeName, ownerId, before, after, description, referenceUrl)
    }
}
