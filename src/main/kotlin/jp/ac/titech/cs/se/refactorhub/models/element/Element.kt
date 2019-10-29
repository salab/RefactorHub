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

    val incomplete get() = location.path.isEmpty() && location.range.isEmpty()

    class Deserializer : JsonDeserializer<Element>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Element {
            val mapper = p.codec as ObjectMapper
            val root = mapper.readTree<ObjectNode>(p)
            val type = Type.valueOf(root["type"].asText())
            return mapper.readValue(root.toString(), type.dataClass.java)
        }
    }

    enum class Type(val dataClass: KClass<out Element>) {
        Empty(jp.ac.titech.cs.se.refactorhub.models.element.impl.Empty::class),
        ClassDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.ClassDeclaration::class),
        ConstructorDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.ConstructorDeclaration::class),
        FieldDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.FieldDeclaration::class),
        InterfaceDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.InterfaceDeclaration::class),
        MethodDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.MethodDeclaration::class),
        MethodInvocation(jp.ac.titech.cs.se.refactorhub.models.element.impl.MethodInvocation::class),
        ParameterDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.ParameterDeclaration::class),
        Statements(jp.ac.titech.cs.se.refactorhub.models.element.impl.Statements::class),
        VariableDeclaration(jp.ac.titech.cs.se.refactorhub.models.element.impl.VariableDeclaration::class)
    }
}
