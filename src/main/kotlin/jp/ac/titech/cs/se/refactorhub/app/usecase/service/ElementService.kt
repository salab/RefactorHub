package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
import org.koin.core.KoinComponent

class ElementService : KoinComponent {

    fun getTypes(): List<String> {
        return CodeElementType.values().map { it.name }
    }

    fun getSchemas(): List<JsonSchema> {
        return CodeElementType.values().map {
            JsonSchemaGenerator(jacksonObjectMapper()).generateSchema(it.klass.java)
        }
    }
}
