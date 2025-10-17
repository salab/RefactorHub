package jp.ac.titech.cs.se.refactorhub.app.infrastructure

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.serialization.jackson.jackson
import io.ktor.server.locations.Locations
import io.ktor.server.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.server.sessions.SessionStorageMemory
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.Session
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth.github
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.connectDB
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature.SinglePageApplication
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature.setExceptions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.module.KoinModule
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.router.root
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = EngineMain.main(args)

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
        modules(KoinModule.modules)
    }

    connectDB()

    routing {
        root()
    }
}
