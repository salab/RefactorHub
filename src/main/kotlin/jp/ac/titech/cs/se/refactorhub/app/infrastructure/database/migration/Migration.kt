package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Actions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitContents
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Experiments
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDrafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringToRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypeDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.UserDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Users
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.annotator.format
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.dsl.module

// TODO
fun main() {
    startKoin {
        modules(
            module {
                single { jacksonObjectMapper() }
            }
        )
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

        initialize()
    }
}

fun initialize() {
    dropTables()
    createTables()

    createRefactoringTypes()
    createUsers()
    createExperiments()
}

private fun dropTables() {
    SchemaUtils.drop(
        Commits,
        Users,
        Refactorings,
        RefactoringToRefactorings,
        RefactoringTypes,
        RefactoringDrafts,
        Experiments,
        ExperimentRefactorings,
        CommitContents,
        Actions
    )
}

private fun createTables() {
    SchemaUtils.create(
        Commits,
        Users,
        Refactorings,
        RefactoringToRefactorings,
        RefactoringTypes,
        RefactoringDrafts,
        Experiments,
        ExperimentRefactorings,
        CommitContents,
        Actions
    )
}

private fun createUsers() {
    UserDao.new {
        this.name = "admin"
        this.subId = 1
    }
}

private fun createRefactoringTypes() {
    val files = listOf("types/refminer.json")

    for (file in files) {
        val stream = object {}.javaClass.classLoader.getResourceAsStream(file) ?: return
        val types = jacksonObjectMapper().readValue<List<RefactoringTypeBody>>(stream)
        types.forEach {
            RefactoringTypeDao.new {
                this.name = it.name
                this.before = it.before
                this.after = it.after
                this.description = it.description
                this.url = it.url
            }
        }
    }
}

private fun createExperiments() {
    createExperiment(
        "Experiment (type)",
        "Experiment that input data has only refactoring type",
        "data/type.ndjson"
    )
    createExperiment(
        "Experiment (description)",
        "Experiment that input data has refactoring type, description",
        "data/description.ndjson"
    )
}

private fun createExperiment(title: String, description: String, input: String) {
    ExperimentDao.new {
        this.title = title
        this.description = description
        this.isActive = true
    }.apply {
        this.refactorings = SizedCollection(createRefactorings(input))
    }
}

private fun createRefactorings(file: String): List<RefactoringDao> {
    val ndjson = object {}.javaClass.classLoader.getResource(file)?.readText() ?: return emptyList()
    val mapper = jacksonObjectMapper()
    val refactorings = ndjson.trim().split("\n").map { mapper.readValue<RefactoringBody>(it) }

    val admin = UserDao[1]
    return refactorings.map {
        val commit = CommitDao.find {
            Commits.sha eq it.commit.sha
        }.firstOrNull() ?: CommitDao.new {
            this.sha = it.commit.sha
            this.owner = it.commit.owner
            this.repository = it.commit.repository
        }
        val type = RefactoringTypeDao.find { RefactoringTypes.name eq it.type }.first()
        val formatted = it.data.format(type.asModel())
        RefactoringDao.new {
            this.owner = admin
            this.commit = commit
            this.type = type
            this.data = Refactoring.Data(formatted.before, formatted.after)
            this.description = it.description
            this.isVerified = false
        }
    }
}

data class RefactoringTypeBody(
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String = "",
    val url: String = ""
)

data class RefactoringBody(
    val commit: Commit,
    val type: String,
    val data: Refactoring.Data,
    val description: String
)
