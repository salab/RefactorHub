package jp.ac.titech.cs.se.refactorhub.core.model.refactoring

import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder

interface Refactoring {
    val commit: Commit
    val type: String
    val data: Data
    val description: String

    interface Data {
        val before: MutableMap<String, CodeElementHolder>
        val after: MutableMap<String, CodeElementHolder>
    }
}
