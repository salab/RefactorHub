package jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.server.locations.Location
import io.ktor.server.locations.location
import io.ktor.server.locations.locations
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.kohsuke.github.GitHub
import org.koin.java.KoinJavaComponent.inject

private const val GITHUB = "github"

@Location("/login")
private class Login

fun AuthenticationConfig.github() {
    oauth(GITHUB) {
        client = HttpClient(Apache)
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name = GITHUB,
                authorizeUrl = "https://github.com/login/oauth/authorize",
                accessTokenUrl = "https://github.com/login/oauth/access_token",
                clientId = application.environment.config.property("ktor.oauth.clientId").getString(),
                clientSecret = application.environment.config.property("ktor.oauth.clientSecret").getString()
            )
        }
        urlProvider = {
            redirectUrl(Login())
        }
    }
}

private fun <T : Any> ApplicationCall.redirectUrl(t: T): String {
    val host = request.host()
    val path = application.locations.href(t)
    return when (host) {
        "localhost" -> "http://$host${request.port().let { if (it == 80) "" else ":$it" }}$path"
        else -> "https://$host$path"
    }
}

fun Route.login() {
    authenticate(GITHUB) {
        location<Login> {
            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                if (principal != null) {
                    val me = getMe(principal.accessToken)
                    call.sessions.set(
                        Session(
                            me.id.toString(),
                            principal.accessToken
                        )
                    )
                    call.respondRedirect("/")
                } else {
                    // TODO
                    call.respondText("Failed")
                }
            }
        }
    }
}

private fun getMe(accessToken: String): User {
    val userService:UserService by inject(UserService::class.java)
    val me = GitHub.connectUsingOAuth(accessToken).myself
    return userService.createIfNotExist(me.id.toInt(), me.login)
}
