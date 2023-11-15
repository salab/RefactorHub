package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration.initializeDatabase
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.net.URI

@KtorExperimentalAPI
fun Application.connectDB() {
    val uri = URI(environment.config.property("ktor.database.url").getString())
    val (username, password) = uri.userInfo.split(":")
    val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
    val dataSource =
        HikariDataSource(
            HikariConfig().apply {
                this.driverClassName = "org.postgresql.Driver"
                this.jdbcUrl = jdbcUrl
                this.username = username
                this.password = password
                validate()
            }
        )
    Database.connect(dataSource)

    val flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .locations("jp/ac/titech/cs/se/refactorhub/app/infrastructure/database/migration")
        .baselineVersion("1.0") // latest
        .load()

    val info = flyway.info()
//    if (info.current() == null) {
        flyway.clean()
        initializeDatabase()
        flyway.baseline()
//    }
    flyway.migrate()
}
