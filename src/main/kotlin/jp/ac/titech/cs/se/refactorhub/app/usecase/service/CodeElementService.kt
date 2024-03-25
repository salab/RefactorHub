package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class CodeElementService : KoinComponent {

    fun getTypes(): List<String> {
        return CodeElementType.values().map { it.name }
    }
}
