package jp.ac.titech.cs.se.refactorhub.tool.model

data class Commit(
    val sha: String,
    var owner: String,
    var repository: String
)
