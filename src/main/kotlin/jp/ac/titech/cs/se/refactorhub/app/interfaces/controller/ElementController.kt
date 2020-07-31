package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ElementService
import org.koin.core.KoinComponent
import org.koin.core.inject

class ElementController : KoinComponent {
    private val elementService: ElementService by inject()

    fun getTypes(): List<String> {
        return elementService.getTypes()
    }

    fun getSchemas(): List<JsonSchema> {
        return elementService.getSchemas()
    }
}
