package jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill

import jp.ac.titech.cs.se.refactorhub.tool.model.DiffCategory

data class Follow(
    val category: DiffCategory,
    val key: String
)
