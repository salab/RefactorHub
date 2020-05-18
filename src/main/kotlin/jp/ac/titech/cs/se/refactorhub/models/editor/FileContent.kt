package jp.ac.titech.cs.se.refactorhub.models.editor

import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class FileContent(
    val value: String = "",
    val language: String? = null,
    val uri: String,
    val elements: List<Element> = listOf()
)