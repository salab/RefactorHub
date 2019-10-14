package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.models.User
import jp.ac.titech.cs.se.refactorhub.repositories.DraftRepository
import org.springframework.stereotype.Service

@Service
class DraftService(
    private val draftRepository: DraftRepository,
    private val userService: UserService
) {

    fun get(id: Long): Draft {
        val draft = draftRepository.findById(id)
        if (draft.isPresent) return draft.get()
        throw NotFoundException("Draft(id=$id) is not found.")
    }

    fun getByOwner(id: Long): Draft {
        val draft = draftRepository.findById(id)
        val owner = userService.me()
        if (draft.isPresent) {
            return draft.get().also {
                if (it.owner != owner) throw ForbiddenException("User(id=${owner.id}) is not Draft(id=$id)'s owner.")
            }
        }
        throw NotFoundException("Draft(id=$id) is not found.")
    }

    fun create(origin: Annotation) = draftRepository.save(
        Draft(
            origin.owner,
            origin.commit,
            origin.parent,
            origin,
            origin.type,
            origin.refactoring,
            origin.description
        )
    )

    fun fork(owner: User, parent: Annotation) = draftRepository.save(
        Draft(
            owner,
            parent.commit,
            parent,
            null,
            parent.type,
            parent.refactoring,
            parent.description
        )
    )

    fun save(draft: Draft) = draftRepository.save(draft)

    fun delete(id: Long) = draftRepository.deleteById(id)

}
