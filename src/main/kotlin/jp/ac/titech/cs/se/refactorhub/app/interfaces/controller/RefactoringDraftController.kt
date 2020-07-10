package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import org.koin.core.KoinComponent

@KtorExperimentalLocationsAPI
@Location("/drafts")
class RefactoringDraftController : KoinComponent {

    @Location("/{id}")
    data class GetDraft(val id: Int)

    @Location("/{id}/save")
    data class SaveDraft(val id: Int)

    @Location("/{id}/discard")
    data class DiscardDraft(val id: Int)

    @Location("/{id}")
    data class UpdateDraft(val id: Int)
    data class UpdateDraftBody(
        val description: String? = null,
        val type: String? = null
    )

    @Location("/{id}/{category}")
    data class PutDraftElementKey(val id: Int, val category: String)
    data class PutDraftElementKeyBody(val key: String)

    @Location("/{id}/{category}/{key}")
    data class RemoveDraftElementKey(val id: Int, val category: String, val key: String)

    @Location("/{id}/{category}/{key}")
    data class AppendDraftElementValue(val id: Int, val category: String, val key: String)

    @Location("/{id}/{category}/{key}/{index}")
    data class UpdateDraftElementValue(val id: Int, val category: String, val key: String, val index: Int)
    data class UpdateDraftElementValueBody(
        val element: CodeElement
    )

    @Location("/{id}/{category}/{key}/{index}")
    data class RemoveDraftElementValue(val id: Int, val category: String, val key: String, val index: Int)

    fun get(params: GetDraft): RefactoringDraft {
        TODO()
    }

    fun save(params: SaveDraft): RefactoringDraft {
        TODO()
    }

    fun discard(params: DiscardDraft) {
        TODO()
    }

    fun update(params: UpdateDraft, body: UpdateDraftBody): RefactoringDraft {
        TODO()
    }

    fun putElementKey(params: PutDraftElementKey, body: PutDraftElementKeyBody): RefactoringDraft {
        TODO()
    }

    fun removeElementKey(params: RemoveDraftElementKey): RefactoringDraft {
        TODO()
    }

    fun appendElementValue(params: AppendDraftElementValue): RefactoringDraft {
        TODO()
    }

    fun updateElementValue(params: UpdateDraftElementValue, body: UpdateDraftElementValueBody): RefactoringDraft {
        TODO()
    }

    fun removeElementValue(params: RemoveDraftElementValue): RefactoringDraft {
        TODO()
    }
}
