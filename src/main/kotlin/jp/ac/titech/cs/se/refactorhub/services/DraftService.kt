package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.models.User
import jp.ac.titech.cs.se.refactorhub.repositories.DraftRepository
import org.springframework.stereotype.Service

@Service
class DraftService(
    private val draftRepository: DraftRepository
) {

    fun get(id: Long): Draft {
        val draft = draftRepository.findById(id)
        if (draft.isPresent) return draft.get()
        throw NotFoundException("Draft(id=$id) is not found.")
    }

    fun create(owner: User, commit: Commit) = draftRepository.save(Draft(owner, commit))

    fun fork(owner: User, parent: Annotation) = draftRepository.save(
        Draft(
            owner,
            parent.commit,
            parent,
            parent.type,
            parent.refactoring,
            parent.description
        )
    )

    fun save(draft: Draft) = draftRepository.save(draft)

    fun delete(id: Long) = draftRepository.deleteById(id)

}
