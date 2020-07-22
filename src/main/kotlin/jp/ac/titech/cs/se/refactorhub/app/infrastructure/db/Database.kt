package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringDrafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringToRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
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
                username = environment.config.property("ktor.database.user").getString()
                password = environment.config.property("ktor.database.password").getString()
                validate()
            }
        )
    )
    transaction {
        addLogger(StdOutSqlLogger)
        create(Commits, Users, Refactorings, RefactoringToRefactorings, RefactoringTypes, RefactoringDrafts)
    }
}
