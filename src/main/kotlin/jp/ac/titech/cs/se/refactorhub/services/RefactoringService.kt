package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.repositories.RefactoringRepository
import org.springframework.stereotype.Service

@Service
class RefactoringService(
    private val refactoringRepository: RefactoringRepository,
    private val draftService: DraftService,
    private val userService: UserService,
    private val commitService: CommitService,
    private val refactoringTypeService: RefactoringTypeService
) {

    fun add(
        type: String,
        description: String,
        commit: Commit,
        data: Refactoring.Data
    ): Refactoring {
        return save(
            Refactoring(
                owner = userService.me(),
                commit = commitService.create(commit.sha, commit.owner, commit.repository),
                type = refactoringTypeService.getByName(type),
                description = description,
                data = data
            )
        )
    }

    fun getAll(): List<Refactoring> = refactoringRepository.findAll()

    fun get(id: Long): Refactoring {
        val optional = refactoringRepository.findById(id)
        if (optional.isPresent) return optional.get()
        throw NotFoundException("Refactoring(id=$id) is not found.")
    }

    fun getChildren(id: Long): List<Refactoring> {
        val optional = refactoringRepository.findByIdAndFetchChildrenEagerly(id)
        if (optional.isPresent) return optional.get().children
        throw NotFoundException("Refactoring(id=$id) is not found.")
    }

    fun getDrafts(id: Long): List<Draft> {
        val optional = refactoringRepository.findByIdAndFetchDraftsEagerly(id)
        if (optional.isPresent) return optional.get().drafts
        throw NotFoundException("Refactoring(id=$id) is not found.")
    }

    fun save(draft: Draft): Refactoring {
        val origin = draft.origin
        val refactoring = if (origin != null) {
            origin.type = draft.type
            origin.data = draft.data
            origin.description = draft.description
            refactoringRepository.save(origin)
        } else refactoringRepository.save(
            Refactoring(
                draft.owner,
                draft.commit,
                draft.parent,
                draft.type,
                draft.data,
                draft.description
            )
        )
        draftService.delete(draft.id)
        return refactoring
    }

    fun save(refactoring: Refactoring): Refactoring = refactoringRepository.save(refactoring)

    fun delete(id: Long) = refactoringRepository.deleteById(id)
}
