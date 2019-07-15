package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.models.Commit
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.stereotype.Service

@Service
class GitHubService(
    private val userService: UserService,
    @Value("\${github.access.token}")
    private val accessToken: String,
    @Qualifier("oauth2ClientContext")
    private val oauth2ClientContext: OAuth2ClientContext
) {

    val github: GitHub
        get() = GitHub.connectUsingOAuth(oauth2ClientContext.accessToken?.value ?: accessToken)

    fun getUser(id: Int): GHUser = github.getUser(userService.get(id).name)

    fun getRepository(owner: String, repository: String): GHRepository = github.getRepository("$owner/$repository")

    fun getCommit(sha: String, owner: String, repository: String): GHCommit =
        getRepository(owner, repository).getCommit(sha)

    fun getCommit(commit: Commit): GHCommit = getCommit(commit.sha, commit.owner, commit.repository)

}