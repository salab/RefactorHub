package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringDraftRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.tool.editor.appendCodeElementValue
import jp.ac.titech.cs.se.refactorhub.tool.editor.changeRefactoringType
import jp.ac.titech.cs.se.refactorhub.tool.editor.putCodeElementKey
import jp.ac.titech.cs.se.refactorhub.tool.editor.removeCodeElementKey
import jp.ac.titech.cs.se.refactorhub.tool.editor.removeCodeElementValue
import jp.ac.titech.cs.se.refactorhub.tool.editor.updateCodeElementValue
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import org.koin.core.KoinComponent
import org.koin.core.inject

class RefactoringDraftService : KoinComponent {
    private val refactoringDraftRepository: RefactoringDraftRepository by inject()
    private val refactoringService: RefactoringService by inject()
    private val refactoringTypeService: RefactoringTypeService by inject()

    fun getRefactoringDrafts(refactoringId: Int): List<RefactoringDraft> {
        return refactoringDraftRepository.findByRefactoringId(refactoringId)
    }

    fun get(id: Int): RefactoringDraft {
        val draft = refactoringDraftRepository.findById(id)
        draft ?: throw NotFoundException("RefactoringDraft(id=$id) is not found")
        return draft
    }

    private fun getByOwner(id: Int, userId: Int?): RefactoringDraft {
        val draft = get(id)
        if (draft.ownerId != userId) {
            throw ForbiddenException("User(id=$userId) is not RefactoringDraft(id=$id)'s owner.")
        }
        return draft
    }

    fun save(id: Int, userId: Int?): Refactoring {
        val draft = getByOwner(id, userId)
        val origin = refactoringService.get(draft.originId)
        val refactoring = if (draft.isFork) {
            refactoringService.create(
                draft.commit,
                draft.type,
                draft.data,
                draft.description,
                draft.ownerId,
                origin.id
            )
        } else {
            refactoringService.update(
                origin.id,
                draft.type,
                draft.data,
                draft.description
            )
        }
        refactoringDraftRepository.deleteById(draft.id)
        return refactoring
    }

    fun discard(id: Int, userId: Int?) {
        val draft = getByOwner(id, userId)
        refactoringDraftRepository.deleteById(draft.id)
    }

    fun update(
        id: Int,
        typeName: String?,
        description: String?,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        if (typeName != null) {
            val type = refactoringTypeService.getByName(typeName)
            val refactoring = changeRefactoringType(type, draft)
            return refactoringDraftRepository.update(
                id,
                refactoring.type,
                Refactoring.Data(
                    refactoring.data.before,
                    refactoring.data.after
                ),
                description
            )
        }
        if (description != null) {
            return refactoringDraftRepository.update(id, description = description)
        }
        return draft
    }

    fun putElementKey(
        id: Int,
        category: String,
        key: String,
        typeName: String,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        val refactoring = try {
            putCodeElementKey(category, key, typeName, draft)
        } catch (e: Exception) {
            throw BadRequestException(e.message!!)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun removeElementKey(
        id: Int,
        category: String,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        val type = refactoringTypeService.getByName(draft.type)
        val refactoring = try {
            removeCodeElementKey(category, key, draft, type)
        } catch (e: Exception) {
            throw BadRequestException(e.message!!)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun appendElementValue(
        id: Int,
        category: String,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        val refactoring = try {
            appendCodeElementValue(category, key, draft)
        } catch (e: Exception) {
            throw BadRequestException(e.message!!)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun updateElementValue(
        id: Int,
        category: String,
        key: String,
        index: Int,
        element: CodeElement,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        val refactoring = try {
            updateCodeElementValue(category, key, index, element, draft)
        } catch (e: Exception) {
            throw BadRequestException(e.message!!)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun removeElementValue(
        id: Int,
        category: String,
        key: String,
        index: Int,
        userId: Int?
    ): RefactoringDraft {
        val draft = getByOwner(id, userId)
        val refactoring = try {
            removeCodeElementValue(category, key, index, draft)
        } catch (e: Exception) {
            throw BadRequestException(e.message!!)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun fork(refactoring: Refactoring, userId: Int): RefactoringDraft {
        return refactoringDraftRepository.create(
            refactoring.type,
            refactoring.commit.sha,
            refactoring.data,
            refactoring.description,
            userId,
            refactoring.id,
            true
        )
    }

    fun edit(refactoring: Refactoring, userId: Int): RefactoringDraft {
        val draft = refactoringDraftRepository.findByRefactoringIdAndOwnerIdAndIsFork(refactoring.id, userId, false)
        return draft ?: refactoringDraftRepository.create(
            refactoring.type,
            refactoring.commit.sha,
            refactoring.data,
            refactoring.description,
            userId,
            refactoring.id,
            false
        )
    }

    fun getUserDrafts(userId: Int): List<RefactoringDraft> {
        return refactoringDraftRepository.findByOwnerId(userId)
    }
}
