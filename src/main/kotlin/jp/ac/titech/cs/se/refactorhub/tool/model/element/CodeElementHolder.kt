package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementHolder(
    val type: String,
    val multiple: Boolean = false,
    val required: Boolean = false,
    val elements: List<CodeElement> = listOf()
)
