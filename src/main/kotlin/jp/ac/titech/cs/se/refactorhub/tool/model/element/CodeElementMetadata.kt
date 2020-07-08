package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementMetadata(
    val type: CodeElementType,
    val multiple: Boolean = false,
    val required: Boolean = false
)
