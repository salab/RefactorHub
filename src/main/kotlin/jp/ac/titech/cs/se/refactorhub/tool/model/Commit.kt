package jp.ac.titech.cs.se.refactorhub.tool.model

interface Commit {
    val sha: String
    val owner: String
    val repository: String
}
