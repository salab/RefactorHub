package jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill

import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory

data class Follow(
    val category: DiffCategory,
    val key: String
)
