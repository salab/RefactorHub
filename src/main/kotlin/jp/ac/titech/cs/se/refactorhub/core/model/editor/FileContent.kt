package jp.ac.titech.cs.se.refactorhub.core.model.editor

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

data class FileContent(
    val value: String = "",
    val language: String? = null,
    val uri: String,
    val elements: List<CodeElement> = listOf()
)
