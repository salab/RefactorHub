package jp.ac.titech.cs.se.refactorhub.tool.model.editor

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement

data class FileContent(
    val value: String = "",
    val language: String? = null,
    val uri: String,
    val elements: List<CodeElement> = listOf()
)
