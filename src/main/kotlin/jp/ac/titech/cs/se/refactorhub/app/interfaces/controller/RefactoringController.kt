package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
import org.koin.core.KoinComponent

class RefactoringController : KoinComponent {

    data class CreateRefactoringBody(
        val type: String,
        val description: String,
        val commit: Commit,
        val data: Refactoring.Data
    )

    fun create(body: CreateRefactoringBody): Refactoring {
        TODO()
    }

    fun getAll(): List<Refactoring> {
        TODO()
    }

    fun get(id: Int): Refactoring {
        TODO()
    }

    fun getChildren(id: Int): Refactoring {
        TODO()
    }

    fun getDrafts(id: Int): Refactoring {
        TODO()
    }

    fun fork(id: Int): Refactoring {
        TODO()
    }

    fun edit(id: Int): Refactoring {
        TODO()
    }
}
