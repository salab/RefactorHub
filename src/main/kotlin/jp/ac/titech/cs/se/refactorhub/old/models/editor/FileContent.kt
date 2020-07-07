package jp.ac.titech.cs.se.refactorhub.old.models.editor

import jp.ac.titech.cs.se.refactorhub.old.models.element.Element

data class FileContent(
    val value: String = "",
    val language: String? = null,
    val uri: String,
    val elements: List<Element> = listOf()
)
