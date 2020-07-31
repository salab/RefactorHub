package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import io.ktor.features.NotFoundException
import jp.ac.titech.cs.se.refactorhub.app.exception.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import jp.ac.titech.cs.se.refactorhub.tool.editor.fixRefactoringData
import org.koin.core.KoinComponent
import org.koin.core.inject

class RefactoringService : KoinComponent {
    private val refactoringRepository: RefactoringRepository by inject()
    private val userService: UserService by inject()
    private val commitService: CommitService by inject()
    private val refactoringDraftService: RefactoringDraftService by inject()
    private val refactoringTypeService: RefactoringTypeService by inject()

    fun create(
        commit: Commit,
        typeName: String,
        data: Refactoring.Data,
        description: String,
        userId: Int?,
        parentId: Int? = null
    ): Refactoring {
        val user = userService.getMe(userId)
        val type = refactoringTypeService.getByName(typeName)
        val sha = commitService.createIfNotExist(commit.sha, commit.owner, commit.repository).sha
        val fixed = fixRefactoringData(type, data)
        return refactoringRepository.create(
            sha,
            type.name,
            Refactoring.Data(
                fixed.before,
                fixed.after
            ),
            description,
            user.id,
            parentId
        )
    }

    fun getAll(): List<Refactoring> {
        return refactoringRepository.findAll()
    }

    fun get(id: Int): Refactoring {
        val refactoring = refactoringRepository.findById(id)
        refactoring ?: throw NotFoundException("Refactoring(id=$id) is not found")
        return refactoring
    }

    fun getChildren(id: Int): List<Refactoring> {
        return refactoringRepository.findByParentId(id)
    }

    fun getDrafts(id: Int): List<RefactoringDraft> {
        return refactoringDraftService.getRefactoringDrafts(id)
    }

    fun update(id: Int, typeName: String?, data: Refactoring.Data?, description: String?): Refactoring {
        return refactoringRepository.update(id, typeName, data, description)
    }

    fun fork(id: Int, userId: Int?): RefactoringDraft {
        val user = userService.getMe(userId)
        val refactoring = get(id)
        return refactoringDraftService.fork(refactoring, user.id)
    }

    fun edit(id: Int, userId: Int?): RefactoringDraft {
        val user = userService.getMe(userId)
        val refactoring = get(id)
        if (refactoring.ownerId != user.id) throw ForbiddenException("You are not Refactoring(id=${refactoring.id})'s owner")
        return refactoringDraftService.edit(refactoring, user.id)
    }

    fun getUserRefactorings(userId: Int): List<Refactoring> {
        return refactoringRepository.findByOwnerId(userId)
    }
}
