package jp.ac.titech.cs.se.refactorhub.app.model

data class Experiment(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val description: String,
    val isActive: Boolean
)
