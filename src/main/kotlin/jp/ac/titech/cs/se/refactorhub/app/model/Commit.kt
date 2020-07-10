package jp.ac.titech.cs.se.refactorhub.app.model

import java.util.Date

data class CommitDetail(
    val sha: String,
    val owner: String,
    val repository: String,
    val url: String,
    val message: String,
    val author: String,
    val authorDate: Date,
    val files: List<CommitFile>,
    val parent: String
)

data class CommitFile(
    val sha: String,
    val status: String,
    val name: String,
    val previousName: String
)
