package jp.ac.titech.cs.se.refactorhub.models.element

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import jp.ac.titech.cs.se.refactorhub.models.element.data.Location
import java.io.Serializable
import kotlin.reflect.KClass

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = Element.Deserializer::class)
interface Element : Serializable {
    val type: Type
    val location: Location

    class Deserializer : JsonDeserializer<Element>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Element {
            val mapper = p.codec as ObjectMapper
            val root = mapper.readTree<ObjectNode>(p)
            val type = Type.valueOf(root["type"].asText())
            return mapper.readValue(root.toString(), type.dataClass.java)
        }
    }

    enum class Type(val dataClass: KClass<out Element>)

}
