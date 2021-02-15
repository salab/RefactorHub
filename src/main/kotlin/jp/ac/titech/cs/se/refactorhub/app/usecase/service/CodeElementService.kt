package jp.ac.titech.cs.se.refactorhub.app.usecase.service

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class CodeElementService : KoinComponent {

    fun getTypes(): List<String> {
        return CodeElementType.values().map { it.name }
    }

    fun getSchemas(): List<JsonSchema> {
        return CodeElementType.values().map {
            JsonSchemaGenerator(jacksonObjectMapper()).generateSchema(it.klass.java)
        }
    }
}
