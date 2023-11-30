package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CodeElementService
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class CodeElementController : KoinComponent {
    private val codeElementService: CodeElementService by inject()

    fun getTypes(): List<String> {
        return codeElementService.getTypes()
    }
}
