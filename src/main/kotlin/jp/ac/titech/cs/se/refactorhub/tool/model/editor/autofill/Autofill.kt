package jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AutofillDeserializer::class)
interface Autofill {
    val type: AutofillType
    val follows: List<Follow>
}
