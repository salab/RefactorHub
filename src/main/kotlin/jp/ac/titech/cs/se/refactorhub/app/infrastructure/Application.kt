package jp.ac.titech.cs.se.refactorhub.app.infrastructure

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.util.KtorExperimentalAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.github
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.connectDB
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature.SinglePageApplication
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature.setExceptions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.module.koinModules
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.root
import org.koin.ktor.ext.Koin

fun main(args: Array<String>) = EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.module() {
    install(AutoHeadResponse)
    install(CallLogging)
    install(Locations)

    install(Sessions) {
        cookie<Session>(Session.KEY, SessionStorageMemory())
    }
    install(Authentication) {
        github()
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(SinglePageApplication)

    install(StatusPages) {
        setExceptions()
    }

    install(Koin) {
        modules(koinModules)
    }

    connectDB()

    routing {
        root()
    }
}
