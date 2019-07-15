package jp.ac.titech.cs.se.refactorhub.models.refactoring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.models.element.ElementSet
import java.io.Serializable
import javax.persistence.AttributeConverter
import kotlin.reflect.KClass

@JsonIgnoreProperties(ignoreUnknown = true)
interface Refactoring : Serializable {
    val type: Type
    val before: ElementSet
    val after: ElementSet

    class Converter : AttributeConverter<Refactoring, String> {
        override fun convertToDatabaseColumn(attribute: Refactoring): String =
            jacksonObjectMapper().writeValueAsString(attribute)

        override fun convertToEntityAttribute(dbData: String): Refactoring {
            val map = jacksonObjectMapper().readValue<Map<String, Any>>(dbData)
            val type: Type = Type.valueOf(map.getOrDefault("type", Type.Custom.name) as String)
            return jacksonObjectMapper().convertValue(map, type.dataClass.java)
        }
    }

    enum class Type(val dataClass: KClass<out Refactoring>) {
        Custom(jp.ac.titech.cs.se.refactorhub.models.refactoring.impl.Custom::class)
    }

}
