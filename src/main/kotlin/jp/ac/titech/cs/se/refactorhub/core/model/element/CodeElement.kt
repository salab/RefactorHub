package jp.ac.titech.cs.se.refactorhub.core.model.element

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CodeElementDeserializer::class)
interface CodeElement {
    val type: CodeElementType
    val location: Location?
}

interface CodeElementInClass : CodeElement {
    val className: String?
}

interface CodeElementInMethod : CodeElementInClass {
    val methodName: String?
}
