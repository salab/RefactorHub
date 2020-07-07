package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementHolder(
    val type: String,
    val multiple: Boolean,
    val required: Boolean,
    val elements: List<CodeElement>
)
