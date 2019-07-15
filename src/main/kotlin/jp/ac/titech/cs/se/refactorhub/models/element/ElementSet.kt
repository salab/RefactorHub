package jp.ac.titech.cs.se.refactorhub.models.element

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
interface ElementSet : Serializable {
    val custom: MutableMap<String, Element>
}
