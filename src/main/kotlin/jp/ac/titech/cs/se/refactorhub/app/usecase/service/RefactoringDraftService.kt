package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.databind.ObjectMapper
import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringDraftRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.core.annotator.appendCodeElementValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.changeRefactoringType
import jp.ac.titech.cs.se.refactorhub.core.annotator.putCodeElementKey
import jp.ac.titech.cs.se.refactorhub.core.annotator.removeCodeElementKey
import jp.ac.titech.cs.se.refactorhub.core.annotator.removeCodeElementValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.updateCodeElementValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.verifyCodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.LogEvent
import jp.ac.titech.cs.se.refactorhub.core.model.LogType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringDraftService : KoinComponent {
    private val refactoringDraftRepository: RefactoringDraftRepository by inject()
    private val refactoringService: RefactoringService by inject()
    private val refactoringTypeService: RefactoringTypeService by inject()
    private val editorService: EditorService by inject()
    private val logService: LogService by inject()
    private val mapper: ObjectMapper by inject()

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
        logService.log(
            LogEvent.Save,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val origin = refactoringService.get(draft.originId)
        val refactoring = if (draft.isFork) {
            refactoringService.create(
                draft.commit,
                draft.type,
                draft.data,
                draft.description,
                draft.ownerId,
                origin.id,
                true
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
        logService.log(
            LogEvent.Discard,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        refactoringDraftRepository.deleteById(draft.id)
    }

    fun update(
        id: Int,
        typeName: String?,
        description: String?,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.Update,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("typeName", typeName)
                put("description", description)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        if (typeName != null) {
            val type = refactoringTypeService.getByName(typeName)
            val refactoring = changeRefactoringType(draft, type)
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
        category: DiffCategory,
        key: String,
        typeName: String,
        multiple: Boolean,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.PutElementKey,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
                put("typeName", typeName)
                put("multiple", multiple)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val refactoring = try {
            putCodeElementKey(draft, category, key, typeName, multiple)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
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
        category: DiffCategory,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.RemoveElementKey,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val type = refactoringTypeService.getByName(draft.type)
        val refactoring = try {
            removeCodeElementKey(draft, type, category, key)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.update(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun verifyElement(
        id: Int,
        category: DiffCategory,
        key: String,
        state: Boolean,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.VerifyElement,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
                put("state", state)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val refactoring = try {
            verifyCodeElement(draft, category, key, state)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
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
        category: DiffCategory,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.AppendElementValue,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val refactoring = try {
            appendCodeElementValue(draft, category, key)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
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
        category: DiffCategory,
        key: String,
        index: Int,
        element: CodeElement,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.UpdateElementValue,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
                put("index", index)
                put("element", mapper.writeValueAsString(element))
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val refactoring = try {
            val type = refactoringTypeService.getByName(draft.type)
            val contents =
                editorService.getCommitFileContents(draft.commit.sha, draft.commit.owner, draft.commit.repository)
            updateCodeElementValue(draft, category, key, index, element, type, contents)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
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
        category: DiffCategory,
        key: String,
        index: Int,
        userId: Int?
    ): RefactoringDraft {
        logService.log(
            LogEvent.RemoveElementValue,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
                put("index", index)
            },
            userId
        )
        val draft = getByOwner(id, userId)
        val refactoring = try {
            removeCodeElementValue(draft, category, key, index)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
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
        logService.log(
            LogEvent.Fork,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", refactoring.id)
            },
            userId
        )
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
        logService.log(
            LogEvent.Edit,
            LogType.Server,
            mapper.createObjectNode().apply {
                put("id", refactoring.id)
            },
            userId
        )
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
