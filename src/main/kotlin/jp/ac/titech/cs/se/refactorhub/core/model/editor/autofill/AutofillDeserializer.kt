package jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

class AutofillDeserializer : JsonDeserializer<Autofill>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Autofill {
        val mapper = p.codec as ObjectMapper
        val root = mapper.readTree<ObjectNode>(p)
        val type = AutofillType.valueOf(root["type"].asText())
        return mapper.readValue(root.toString(), type.klass.java)
    }
}
