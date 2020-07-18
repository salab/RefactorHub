package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import org.koin.core.KoinComponent

class ElementService : KoinComponent {

    fun getTypes(): List<String> {
        return CodeElementType.values().map { it.name }
    }
}
