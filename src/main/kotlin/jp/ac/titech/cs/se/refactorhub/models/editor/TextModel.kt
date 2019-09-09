package jp.ac.titech.cs.se.refactorhub.models.editor

import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class TextModel(
    val value: String = "",
    val language: String? = null,
    val uri: String? = null,
    val elements: List<Element> = listOf()
)