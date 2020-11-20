package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalAPI
fun Application.connectDB() {
    Database.connect(
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = environment.config.property("ktor.database.driver").getString()
                jdbcUrl = environment.config.property("ktor.database.url").getString()
                username = environment.config.property("ktor.database.username").getString()
                password = environment.config.property("ktor.database.password").getString()
                validate()
            }
        )
    )
    transaction {
        addLogger(StdOutSqlLogger)
    }
}
