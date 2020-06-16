package jp.ac.titech.cs.se.miner2hub.converter

import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.element.Element

data class RefactoringOutput(
    val type: String,
    val description: String,
    val commit: RefactoringMetadata.Commit,
    val data: Data
) {
    data class Data(
        val before: Map<String, Element.Data>,
        val after: Map<String, Element.Data>
    )
}