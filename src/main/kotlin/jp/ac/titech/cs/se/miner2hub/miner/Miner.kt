package jp.ac.titech.cs.se.miner2hub.miner

import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import org.refactoringminer.api.Refactoring
import org.refactoringminer.api.RefactoringHandler
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl
import org.refactoringminer.util.GitServiceImpl

private const val OUTPUTS_PATH = "outputs"
private const val REPOS_PATH = "$OUTPUTS_PATH/repos"

object Miner {
    private fun detectAtCommit(owner: String, repo: String, sha: String, callback: (List<Refactoring>) -> Unit) {
        GitServiceImpl().cloneIfNotExists("$REPOS_PATH/$repo/repo", "https://github.com/$owner/$repo.git")
            .use { repository ->
                val miner = GitHistoryRefactoringMinerImpl()
                miner.detectAtCommit(repository, sha, Handler(callback))
            }
    }

    fun reDetect(metadata: RefactoringMetadata, handler: (Refactoring) -> Unit) {
        detectAtCommit(metadata.commit.owner, metadata.commit.repo, metadata.commit.sha) { refs ->
            handler(refs.find { it.toString().replace("\t", " ") == metadata.description }
                ?: throw RuntimeException("Failed to re-detect: ${metadata.description}"))
        }
    }
}

private class Handler(private val handle: (List<Refactoring>) -> Unit) : RefactoringHandler() {
    override fun handle(commitId: String, refactorings: List<Refactoring>) = handle(refactorings)
}
