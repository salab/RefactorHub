package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeDatabase() {
    transaction {
        createTables()
        createInitialData()
    }
}

private val tables = arrayOf(
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

private fun createTables() {
    SchemaUtils.create(*tables)
}

private fun createInitialData() {
    createUsers()
    createRefactoringTypes()
    createTutorial()
    createExperiments()
}

private fun createUsers() {
    UserDao.new {
        this.name = "admin"
        this.subId = 1
    }
}

private fun createRefactoringTypes() {
    val files = listOf("types/experiment.json", "types/experiment-wo-autofill.json")

    val admin = UserDao[1]
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
                this.owner = admin
            }
        }
    }
}

private fun createTutorial() {
    createExperiment(
        "Tutorial",
        "Tutorial",
        "data/tutorial.ndjson"
    )
}

private fun createExperiments() {
    createExperiment(
        "Experiment 1",
        "Experiment that input data has refactoring type, description",
        "data/experiment-1.ndjson"
    )
    createExperiment(
        "Experiment 2",
        "Experiment that input data does not have anything",
        "data/experiment-2.ndjson"
    )
    (1..4).forEach {
        createExperiment(
            "Experiment 1-$it",
            "Experiment that input data has refactoring type, description",
            "data/experiment/1/$it.ndjson"
        )
        createExperiment(
            "Experiment 2-$it",
            "Experiment that input data does not have anything",
            "data/experiment/2/$it.ndjson"
        )
    }
}

private fun createExperiment(title: String, description: String, input: String) {
    val admin = UserDao[1]
    ExperimentDao.new {
        this.title = title
        this.description = description
        this.isActive = true
        this.owner = admin
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

private data class RefactoringTypeBody(
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String = "",
    val url: String = ""
)

private data class RefactoringBody(
    val commit: Commit,
    val type: String,
    val data: Refactoring.Data,
    val description: String
)
