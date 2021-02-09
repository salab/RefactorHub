package jp.ac.titech.cs.se.refactorhub.core.model.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

data class FileContent(
    val text: String = "",
    val extension: String = "",
    val uri: String,
    val elements: List<CodeElement> = listOf()
)
