package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.*
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.fetchCommitFromGitHub
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeDatabase() {
    transaction {
        createTables()
        createInitialData()
    }
}

private val tables = arrayOf(
    Users,
    Commits,
    Experiments,
    ChangeTypes,
    Changes,
    Snapshots,
    Annotations,
    Actions
)

private fun createTables() {
    SchemaUtils.create(*tables)
}

private fun createInitialData() {
    val adminDao = createAdmin()
    createChangeTypes(adminDao)
    createTutorials(adminDao)
    createExperiments(adminDao)
}

private fun createAdmin(): UserDao {
    return UserDao.new {
        this.name = "admin"
        this.subId = 1
    }
}

private fun createChangeTypes(adminDao: UserDao) {
    val files = listOf("types/experiment.json")

    for (file in files) {
        val stream = object {}.javaClass.classLoader.getResourceAsStream(file) ?: return
        val types = jacksonObjectMapper().readValue<List<ChangeTypeBody>>(stream)
        types.forEach {
            ChangeTypeDao.new(it.name) {
                this.owner = adminDao
                this.before = it.before
                this.after = it.after
                this.description = it.description
                this.referenceUrl = it.referenceUrl
                this.tags = it.tags
            }
        }
    }
}

private fun createTutorials(adminDao: UserDao) {
    createExperiment(
        adminDao,
        "Tutorial Tool (P)",
        "Tutorial of RefactorHub 2.0 with Tool P",
        "data/tutorial-tool-P.ndjson"
    )
    createExperiment(
        adminDao,
        "Tutorial Tool (Q)",
        "Tutorial of RefactorHub 2.0 with Tool Q",
        "data/tutorial-tool-Q.ndjson"
    )
    createExperiment(
        adminDao,
        "Tutorial Refactoring (Q)",
        "Tutorial of Refactorings with Tool Q",
        "data/tutorial-refactoring.ndjson"
    )
    createExperiment(
        adminDao,
        "Tutorial Exercise (Q)",
        "Tutorial of some changes with Tool Q",
        "data/tutorial-exercise.ndjson"
    )
}

private fun createExperiments(adminDao: UserDao) {
    createExperiment(
        adminDao,
        "Experiment A1 (P)",
        "RefactorHub 2.0 Experiment: Part A, Order 1, with Tool P",
        "data/experiment-A1.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment A1 (Q)",
        "RefactorHub 2.0 Experiment: Part A, Order 1, with Tool Q",
        "data/experiment-A1.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment A2 (P)",
        "RefactorHub 2.0 Experiment: Part A, Order 2, with Tool P",
        "data/experiment-A2.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment A2 (Q)",
        "RefactorHub 2.0 Experiment: Part A, Order 2, with Tool Q",
        "data/experiment-A2.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment A3 (P)",
        "RefactorHub 2.0 Experiment: Part A, Order 3, with Tool P",
        "data/experiment-A3.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment A3 (Q)",
        "RefactorHub 2.0 Experiment: Part A, Order 3, with Tool Q",
        "data/experiment-A3.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B1 (P)",
        "RefactorHub 2.0 Experiment: Part B, Order 1, with Tool P",
        "data/experiment-B1.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B1 (Q)",
        "RefactorHub 2.0 Experiment: Part B, Order 1, with Tool Q",
        "data/experiment-B1.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B2 (P)",
        "RefactorHub 2.0 Experiment: Part B, Order 2, with Tool P",
        "data/experiment-B2.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B2 (Q)",
        "RefactorHub 2.0 Experiment: Part B, Order 2, with Tool Q",
        "data/experiment-B2.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B3 (P)",
        "RefactorHub 2.0 Experiment: Part B, Order 3, with Tool P",
        "data/experiment-B3.ndjson"
    )
    createExperiment(
        adminDao,
        "Experiment B3 (Q)",
        "RefactorHub 2.0 Experiment: Part B, Order 3, with Tool Q",
        "data/experiment-B3.ndjson"
    )
}

private fun createExperiment(adminDao: UserDao, title: String, description: String, input: String) {
    val experimentDao = ExperimentDao.new {
        this.owner = adminDao
        this.title = title
        this.description = description
        this.isActive = true
    }
    createCommits(input, experimentDao)
}

private fun createCommits(file: String, experimentDao: ExperimentDao): List<CommitDao> {
    val ndjson = object {}.javaClass.classLoader.getResource(file)?.readText() ?: return emptyList()
    val mapper = jacksonObjectMapper()
    val commits = ndjson.trim().split("\n").map { mapper.readValue<CommitBody>(it) }

    return commits.indices.map {
        val commit = fetchCommitFromGitHub(commits[it].owner, commits[it].repository, commits[it].sha)
        CommitDao.new {
            this.experiment = experimentDao
            this.orderIndex = it
            this.owner = commit.owner
            this.repository = commit.repository
            this.sha = commit.sha
            this.parentSha = commit.parentSha
            this.url = commit.url
            this.message = commit.message
            this.authorName = commit.authorName
            this.authoredDateTime = commit.authoredDateTime
            this.beforeFiles = commit.beforeFiles
            this.afterFiles = commit.afterFiles
            this.fileMappings = commit.fileMappings
            this.patch = commit.patch
        }
    }
}

private data class ChangeTypeBody (
    val name: String,
    val before: Map<String, CodeElementMetadata>,
    val after: Map<String, CodeElementMetadata>,
    val description: String = "",
    val referenceUrl: String = "",
    val tags: List<String>
)

private data class CommitBody(
    val sha: String,
    val owner: String,
    val repository: String
)
