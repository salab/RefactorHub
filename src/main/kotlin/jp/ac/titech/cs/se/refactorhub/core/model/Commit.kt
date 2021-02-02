package jp.ac.titech.cs.se.refactorhub.core.model

interface Commit {
    val owner: String
    val repository: String
    val sha: String
}
