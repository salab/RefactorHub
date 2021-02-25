package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database

import com.viartemev.ktor.flyway.FlywayFeature
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.util.KtorExperimentalAPI
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

    install(FlywayFeature) {
        this.dataSource = dataSource
        this.locations = arrayOf("jp/ac/titech/cs/se/refactorhub/app/infrastructure/database/migration")
    }
}
