package jp.ac.titech.cs.se.refactorhub.old.models.element.data

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class Range(
    var startLine: Int = 0,
    var startColumn: Int = 0,
    var endLine: Int = 0,
    var endColumn: Int = 0
) : Serializable {
    @JsonIgnore
    fun isEmpty() = startLine == 0 && startColumn == 0 && endLine == 0 && endColumn == 0
}
