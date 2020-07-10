package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/drafts")
class RefactoringDraftController : KoinComponent {

    data class UpdateDraftBody(
        val description: String? = null,
        val type: String? = null
    )

    data class PutDraftElementKeyBody(
        val key: String
    )

    data class UpdateDraftElementValueBody(
        val element: CodeElement
    )

    fun get(id: Int): RefactoringDraft {
        TODO()
    }

    fun save(id: Int): RefactoringDraft {
        TODO()
    }

    fun discard(id: Int) {
        TODO()
    }

    fun update(id: Int, body: UpdateDraftBody): RefactoringDraft {
        TODO()
    }

    fun putElementKey(id: Int, category: String, body: PutDraftElementKeyBody): RefactoringDraft {
        TODO()
    }

    fun removeElementKey(id: Int, category: String, key: String): RefactoringDraft {
        TODO()
    }

    fun appendElementValue(id: Int, category: String, key: String): RefactoringDraft {
        TODO()
    }

    fun updateElementValue(
        id: Int,
        category: String,
        key: String,
        index: Int,
        body: UpdateDraftElementValueBody
    ): RefactoringDraft {
        TODO()
    }

    fun removeElementValue(id: Int, category: String, key: String, index: Int): RefactoringDraft {
        TODO()
    }
}
