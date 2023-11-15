package jp.ac.titech.cs.se.refactorhub.app.model

import java.util.UUID

data class Experiment(
    val id: UUID,
    val ownerId: UUID,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val targetCommits: List<Commit>
)
