package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.databind.ObjectMapper
import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.exception.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringDraftRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.core.annotator.appendCodeElementDefaultValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.changeType
import jp.ac.titech.cs.se.refactorhub.core.annotator.putCodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.annotator.removeCodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.annotator.removeCodeElementValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.updateCodeElementValue
import jp.ac.titech.cs.se.refactorhub.core.annotator.verifyCodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringDraftService : KoinComponent {
    private val refactoringDraftRepository: RefactoringDraftRepository by inject()
    private val refactoringService: RefactoringService by inject()
    private val refactoringTypeService: RefactoringTypeService by inject()
    private val annotatorService: AnnotatorService by inject()
    private val actionService: ActionService by inject()
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
        actionService.log(
            ActionName.Save,
            ActionType.Server,
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
        actionService.log(
            ActionName.Discard,
            ActionType.Server,
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
        actionService.log(
            ActionName.Update,
            ActionType.Server,
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
            val refactoring = draft.changeType(type)
            return refactoringDraftRepository.updateById(
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
            return refactoringDraftRepository.updateById(id, description = description)
        }
        return draft
    }

    fun putCodeElementHolder(
        id: Int,
        category: DiffCategory,
        key: String,
        typeName: String,
        multiple: Boolean,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.PutCodeElementHolder,
            ActionType.Server,
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
            draft.putCodeElementHolder(category, key, typeName, multiple)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun removeCodeElementHolder(
        id: Int,
        category: DiffCategory,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.RemoveCodeElementHolder,
            ActionType.Server,
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
            draft.removeCodeElementHolder(category, key, type)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun verifyCodeElementHolder(
        id: Int,
        category: DiffCategory,
        key: String,
        state: Boolean,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.VerifyCodeElementHolder,
            ActionType.Server,
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
            draft.verifyCodeElementHolder(category, key, state)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun appendCodeElementDefaultValue(
        id: Int,
        category: DiffCategory,
        key: String,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.AppendCodeElementDefaultValue,
            ActionType.Server,
            mapper.createObjectNode().apply {
                put("id", id)
                put("category", category.name)
                put("key", key)
            },
            userId
        )

        val draft = getByOwner(id, userId)
        val refactoring = try {
            draft.appendCodeElementDefaultValue(category, key)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun updateCodeElementValue(
        id: Int,
        category: DiffCategory,
        key: String,
        index: Int,
        element: CodeElement,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.UpdateCodeElementValue,
            ActionType.Server,
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
            val content =
                annotatorService.getCommitContent(draft.commit.owner, draft.commit.repository, draft.commit.sha)
            draft.updateCodeElementValue(category, key, index, element, type, content)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun removeCodeElementValue(
        id: Int,
        category: DiffCategory,
        key: String,
        index: Int,
        userId: Int?
    ): RefactoringDraft {
        actionService.log(
            ActionName.RemoveCodeElementValue,
            ActionType.Server,
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
            draft.removeCodeElementValue(category, key, index)
        } catch (e: Exception) {
            throw BadRequestException(e.message)
        }
        return refactoringDraftRepository.updateById(
            id,
            data = Refactoring.Data(
                refactoring.data.before,
                refactoring.data.after
            )
        )
    }

    fun fork(refactoring: Refactoring, userId: Int): RefactoringDraft {
        actionService.log(
            ActionName.Fork,
            ActionType.Server,
            mapper.createObjectNode().apply {
                put("id", refactoring.id)
            },
            userId
        )

        return refactoringDraftRepository.create(
            refactoring.commit,
            refactoring.type,
            refactoring.data,
            refactoring.description,
            userId,
            refactoring.id,
            true
        )
    }

    fun edit(refactoring: Refactoring, userId: Int): RefactoringDraft {
        actionService.log(
            ActionName.Edit,
            ActionType.Server,
            mapper.createObjectNode().apply {
                put("id", refactoring.id)
            },
            userId
        )

        val draft = refactoringDraftRepository.findByRefactoringIdAndOwnerIdAndIsFork(refactoring.id, userId, false)
        return draft ?: refactoringDraftRepository.create(
            refactoring.commit,
            refactoring.type,
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
