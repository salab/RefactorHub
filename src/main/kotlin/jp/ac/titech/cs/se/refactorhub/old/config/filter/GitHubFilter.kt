package jp.ac.titech.cs.se.refactorhub.old.config.filter

import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import java.util.Locale
import javax.servlet.http.HttpServletRequest

class GitHubFilter : AbstractPreAuthenticatedProcessingFilter() {

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest) = getToken(request)
    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): GHUser? {
        val token = getToken(request)
        if (token === null) return null
        return try {
            GitHub.connectUsingOAuth(token).myself
        } catch (e: Exception) {
            logger.warn("Failed to authenticate", e)
            return null
        }
    }

    private fun getToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader("Authorization")
        val bearerPrefix = "bearer "
        return if (authorization == null || !authorization.toLowerCase(Locale.ENGLISH).startsWith(bearerPrefix)) null
        else authorization.substring(bearerPrefix.length)
    }
}
