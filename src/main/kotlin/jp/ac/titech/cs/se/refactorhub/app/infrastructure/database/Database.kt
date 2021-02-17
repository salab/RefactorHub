package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database

import com.viartemev.ktor.flyway.FlywayFeature
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database

@KtorExperimentalAPI
fun Application.connectDB() {
    val dataSource =
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = environment.config.property("ktor.database.driver").getString()
                jdbcUrl = environment.config.property("ktor.database.url").getString()
                username = environment.config.property("ktor.database.username").getString()
                password = environment.config.property("ktor.database.password").getString()
                validate()
            }
        )
    Database.connect(dataSource)

    install(FlywayFeature) {
        this.dataSource = dataSource
        this.locations = arrayOf("jp/ac/titech/cs/se/refactorhub/app/infrastructure/database/migration")
    }
}
