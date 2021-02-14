package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringDraftService
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefactoringDraftController : KoinComponent {
    private val refactoringDraftService: RefactoringDraftService by inject()

    data class UpdateDraftBody(
        val description: String? = null,
        val type: String? = null
    )

    data class PutCodeElementHolderBody(
        val key: String,
        val type: String,
        val multiple: Boolean
    )

    data class VerifyCodeElementHolderBody(
        val state: Boolean
    )

    data class UpdateCodeElementValueBody(
        val element: CodeElement
    )

    fun get(id: Int): RefactoringDraft {
        return refactoringDraftService.get(id)
    }

    fun save(id: Int, userId: Int?): Refactoring {
        return refactoringDraftService.save(id, userId)
    }

    fun discard(id: Int, userId: Int?) {
        return refactoringDraftService.discard(id, userId)
    }

    fun update(id: Int, body: UpdateDraftBody, userId: Int?): RefactoringDraft {
        return refactoringDraftService.update(id, body.type, body.description, userId)
    }

    fun putCodeElementHolder(
        id: Int,
        category: DiffCategory,
        body: PutCodeElementHolderBody,
        userId: Int?
    ): RefactoringDraft {
        return refactoringDraftService.putCodeElementHolder(id, category, body.key, body.type, body.multiple, userId)
    }

    fun removeCodeElementHolder(id: Int, category: DiffCategory, key: String, userId: Int?): RefactoringDraft {
        return refactoringDraftService.removeCodeElementHolder(id, category, key, userId)
    }

    fun verifyCodeElementHolder(
        id: Int,
        category: DiffCategory,
        key: String,
        state: Boolean,
        userId: Int?
    ): RefactoringDraft {
        return refactoringDraftService.verifyCodeElementHolder(id, category, key, state, userId)
    }

    fun appendCodeElementDefaultValue(id: Int, category: DiffCategory, key: String, userId: Int?): RefactoringDraft {
        return refactoringDraftService.appendCodeElementDefaultValue(id, category, key, userId)
    }

    fun updateCodeElementValue(
        id: Int,
        category: DiffCategory,
        key: String,
        index: Int,
        body: UpdateCodeElementValueBody,
        userId: Int?
    ): RefactoringDraft {
        return refactoringDraftService.updateCodeElementValue(id, category, key, index, body.element, userId)
    }

    fun removeCodeElementValue(
        id: Int,
        category: DiffCategory,
        key: String,
        index: Int,
        userId: Int?
    ): RefactoringDraft {
        return refactoringDraftService.removeCodeElementValue(id, category, key, index, userId)
    }
}
