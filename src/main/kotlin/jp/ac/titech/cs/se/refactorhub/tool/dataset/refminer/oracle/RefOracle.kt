package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.OUTPUTS_PATH
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Commit
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

private const val DATA_JSON_URL = "http://refactoring.encs.concordia.ca/oracle/api.php?json"

private val REPOSITORY_URL_REGEX = Regex("""https://github.com/([\w\-]+)/([\w\-.]+)\.git""")

private fun getDataJson(): String {
    val file = File("$OUTPUTS_PATH/oracle/data.json")
    if (!file.exists()) {
        file.parentFile.mkdirs()
        FileUtils.copyURLToFile(URL(DATA_JSON_URL), file)
    }
    return file.readText()
}

private fun parseDataJson(json: String): List<RefOracleCommit> {
    // Fix wrong escape
    val fixed = json.replace("\\'", "'")
    return jacksonObjectMapper().readValue(fixed)
}

private fun convert(commits: List<RefOracleCommit>): List<List<RefOracleData>> = commits.map { commit ->
    commit.refactorings.filter {
        it.validation == "TP" && it.detectionTools.contains("RefactoringMiner")
    }.map {
        val result = REPOSITORY_URL_REGEX.matchEntire(commit.repository)
            ?: throw RuntimeException("repository url is invalid: ${commit.repository}")
        val (owner, repository) = result.destructured
        RefOracleData(
            it.type,
            it.description,
            Commit(
                commit.sha1,
                owner,
                repository
            )
        )
    }
}

fun getRefOracleDataset(type: String, n: Int, m: Int = 10, random: Boolean = false): List<RefOracleData> {
    val commits = parseDataJson(getDataJson())
    val refactoringLists = convert(commits)

    val candidateLists = refactoringLists.filter { it.size <= m }.map { refs ->
        refs.filter { it.type == type }
    }.filter { it.isNotEmpty() }

    if (n <= candidateLists.size) {
        return (if (random) candidateLists.shuffled() else candidateLists).take(n).map { it.first() }
    }

    val candidates = candidateLists.flatten()
    if (n <= candidates.size) {
        return (if (random) candidates.shuffled() else candidates).take(n)
    }

    println("number of type='$type' is ${candidates.size}")
    return candidates
}

data class RefOracleData(
    val type: String,
    val description: String,
    val commit: Commit
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RefOracleCommit(
    val id: Int,
    val repository: String,
    val sha1: String,
    val refactorings: List<RefOracleRefactoring>
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RefOracleRefactoring(
    val type: String,
    val description: String,
    val validation: String,
    val detectionTools: String
)
