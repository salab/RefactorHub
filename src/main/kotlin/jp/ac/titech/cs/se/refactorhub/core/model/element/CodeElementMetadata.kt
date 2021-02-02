package jp.ac.titech.cs.se.refactorhub.core.model.element

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.Autofill

data class CodeElementMetadata(
    val type: CodeElementType,
    val multiple: Boolean = false,
    val required: Boolean = false,
    val autofills: List<Autofill> = listOf(),
    val description: String = ""
)
