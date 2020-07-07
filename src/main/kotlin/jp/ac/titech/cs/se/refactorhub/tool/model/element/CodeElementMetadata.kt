package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementMetadata(
    val type: String,
    val multiple: Boolean = false,
    val required: Boolean = false
)
