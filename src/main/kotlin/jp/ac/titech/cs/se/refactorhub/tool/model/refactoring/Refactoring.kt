package jp.ac.titech.cs.se.refactorhub.tool.model.refactoring

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder

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
