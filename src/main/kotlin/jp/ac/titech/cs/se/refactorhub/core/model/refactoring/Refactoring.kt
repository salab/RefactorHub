package jp.ac.titech.cs.se.refactorhub.core.model.refactoring

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder

interface Refactoring {
    val type: String
    val commit: Commit
    val data: Data
    val description: String

    interface Data {
        val before: Map<String, CodeElementHolder>
        val after: Map<String, CodeElementHolder>
    }
}
