package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.models.Commit
import org.kohsuke.github.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class GitHubService(
    private val userService: UserService,
    @Value("\${github.access.token:}")
    private val accessToken: String
) {

    val github: GitHub
        get() {
            val credentials = SecurityContextHolder.getContext().authentication?.credentials
            return GitHub.connectUsingOAuth(if (credentials is String) credentials else accessToken)
        }

    fun getUser(id: Long): GHUser = github.getUser(userService.get(id).name)

    fun getRepository(owner: String, repository: String): GHRepository = github.getRepository("$owner/$repository")

    fun getCommit(sha: String, owner: String, repository: String): GHCommit =
        getRepository(owner, repository).getCommit(sha)

    fun getCommit(commit: Commit): GHCommit = getCommit(commit.sha, commit.owner, commit.repository)

    fun getContent(sha: String, owner: String, repository: String, path: String): GHContent =
        getRepository(owner, repository).getFileContent(path, sha)

}