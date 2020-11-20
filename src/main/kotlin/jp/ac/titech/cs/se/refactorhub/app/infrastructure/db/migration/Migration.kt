package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.migration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringDrafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringToRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao.Users
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.CommitRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.RefactoringRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.RefactoringTypeRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.UserRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.CommitRepository
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringRepository
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.RefactoringTypeRepository
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.User
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CommitService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementMetadata
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

// TODO
fun main() {
    startKoin {
        modules(module {
            single { jacksonObjectMapper() }
            single { UserService() }
            single { CommitService() }
            single { RefactoringService() }
            single { RefactoringTypeService() }
            single<UserRepository> { UserRepositoryImpl() }
            single<CommitRepository> { CommitRepositoryImpl() }
            single<RefactoringRepository> { RefactoringRepositoryImpl() }
            single<RefactoringTypeRepository> { RefactoringTypeRepositoryImpl() }
        })
    }

    Database.connect(
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = System.getenv("DATABASE_DRIVER") ?: "org.h2.Driver"
                jdbcUrl = System.getenv("DATABASE_JDBC_URL") ?: "jdbc:h2:mem:test"
                username = System.getenv("DATABASE_USERNAME") ?: "refactorhub"
                password = System.getenv("DATABASE_PASSWORD") ?: "password"
                validate()
            }
        )
    )

    transaction {
        addLogger(StdOutSqlLogger)
        dropTables()
        createTables()
        createRefactoringTypes("types/refminer.json")
        createExperiments()
    }
}

private fun dropTables() {
    SchemaUtils.drop(Commits, Users, Refactorings, RefactoringToRefactorings, RefactoringTypes, RefactoringDrafts)
}

private fun createTables() {
    SchemaUtils.create(Commits, Users, Refactorings, RefactoringToRefactorings, RefactoringTypes, RefactoringDrafts)
}

private fun createRefactoringTypes(file: String) {
    val stream = object {}.javaClass.classLoader.getResourceAsStream(file) ?: return
    val types = jacksonObjectMapper().readValue<List<RefactoringTypeBody>>(stream)
    val refactoringTypeService by inject(RefactoringTypeService::class.java)
    types.forEach { refactoringTypeService.create(it.name, it.before, it.after, it.description) }
}

private fun createExperiments() {
    val userService by inject(UserService::class.java)
    createRefactorings(userService.createIfNotExist(1, "experiment-type"), "data/type.ndjson")
    createRefactorings(userService.createIfNotExist(2, "experiment-desc"), "data/description.ndjson")
}

private fun createRefactorings(user: User, file: String) {
    val ndjson = object {}.javaClass.classLoader.getResource(file)?.readText() ?: return
    val refactorings = ndjson.trim().split("\n").map { jacksonObjectMapper().readValue<RefactoringBody>(it) }
    val refactoringService by inject(RefactoringService::class.java)

    refactorings.forEach {
        refactoringService.create(
            it.commit,
            it.type,
            it.data,
            it.description,
            user.id,
            null
        )
    }
}

data class RefactoringTypeBody(
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String = ""
)

data class RefactoringBody(
    val commit: Commit,
    val type: String,
    val data: Refactoring.Data,
    val description: String
)
