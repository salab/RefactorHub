package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.miner

import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.OUTPUTS_PATH
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import org.refactoringminer.api.Refactoring
import org.refactoringminer.api.RefactoringHandler
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl
import org.refactoringminer.util.GitServiceImpl

private const val REPOS_PATH = "$OUTPUTS_PATH/repos"

private fun detectAtCommit(owner: String, repository: String, sha: String, callback: (List<Refactoring>) -> Unit) {
    GitServiceImpl().cloneIfNotExists("$REPOS_PATH/$repository/repo", "https://github.com/$owner/$repository.git")
        .use {
            val miner = GitHistoryRefactoringMinerImpl()
            miner.detectAtCommit(it, sha, object : RefactoringHandler() {
                override fun handle(commitId: String, refactorings: List<Refactoring>) {
                    callback(refactorings)
                }
            })
        }
}

fun reDetect(data: RefOracleData, handler: (Refactoring) -> Unit) {
    detectAtCommit(data.commit.owner, data.commit.repository, data.commit.sha) { refs ->
        handler(refs.find { it.toString().replace("\t", " ") == data.description }
            ?: throw RuntimeException("Failed to re-detect: ${data.description}"))
    }
}
