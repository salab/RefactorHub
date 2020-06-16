package jp.ac.titech.cs.se.miner2hub.converter

import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class RefactoringOutput(
    val type: String,
    val description: String,
    val url: String,
    val data: Data
) {
    data class Data(
        val before: Map<String, Element.Data>,
        val after: Map<String, Element.Data>
    )
}