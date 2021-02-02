package jp.ac.titech.cs.se.refactorhub.core.model

interface Commit {
    val sha: String
    val owner: String
    val repository: String
}
