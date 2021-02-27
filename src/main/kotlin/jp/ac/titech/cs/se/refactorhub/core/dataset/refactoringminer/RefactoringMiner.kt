package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer

import jp.ac.titech.cs.se.refactorhub.core.dataset.OUTPUT_PATH
import org.refactoringminer.api.Refactoring
import org.refactoringminer.api.RefactoringHandler
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl
import org.refactoringminer.util.GitServiceImpl

object RefactoringMiner {
    private const val REPOS_PATH = "$OUTPUT_PATH/repos"

    private fun detectAtCommit(owner: String, repository: String, sha: String, callback: (List<Refactoring>) -> Unit) {
        GitServiceImpl().cloneIfNotExists("$REPOS_PATH/$repository/repo", "https://github.com/$owner/$repository.git")
            .use {
                val miner = GitHistoryRefactoringMinerImpl()
                miner.detectAtCommit(
                    it,
                    sha,
                    object : RefactoringHandler() {
                        override fun handle(commitId: String, refactorings: List<Refactoring>) {
                            callback(refactorings)
                        }
                    }
                )
            }
    }

    fun reDetect(refactoring: RefactoringOracle.Refactoring, handler: (Refactoring) -> Unit) {
        detectAtCommit(refactoring.commit.owner, refactoring.commit.repository, refactoring.commit.sha) { refs ->
            handler(
                refs.find { it.toString().replace("\t", " ") == refactoring.description }
                    ?: throw RuntimeException("Failed to re-detect: ${refactoring.description}")
            )
        }
    }
}
