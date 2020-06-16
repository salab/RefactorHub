package jp.ac.titech.cs.se.miner2hub.oracle

data class RefactoringMetadata(
    val type: String,
    val description: String,
    val commit: Commit
) {
    data class Commit(
        val owner: String,
        val repo: String,
        val sha: String
    )
}
