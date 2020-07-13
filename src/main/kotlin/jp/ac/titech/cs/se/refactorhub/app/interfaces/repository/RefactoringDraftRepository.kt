package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft

interface RefactoringDraftRepository {
    fun findById(id: Int): RefactoringDraft?
    fun findByOwnerId(ownerId: Int): List<RefactoringDraft>
    fun findByRefactoringId(refactoringId: Int): List<RefactoringDraft>
    fun findByRefactoringIdAndOwnerIdAndIsFork(refactoringId: Int, ownerId: Int, isFork: Boolean): RefactoringDraft?
    fun create(
        typeName: String,
        commitSha: String,
        data: Refactoring.Data,
        description: String,
        userId: Int,
        originId: Int,
        isFork: Boolean
    ): RefactoringDraft

    fun update(
        id: Int,
        typeName: String? = null,
        data: Refactoring.Data? = null,
        description: String? = null
    ): RefactoringDraft

    fun deleteById(id: Int)
}
