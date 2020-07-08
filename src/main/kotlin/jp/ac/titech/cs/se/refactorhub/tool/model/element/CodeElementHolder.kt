package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementHolder(
    val type: CodeElementType,
    val elements: List<CodeElement> = listOf()
)
