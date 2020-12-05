package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.OUTPUTS_PATH
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Commit
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

private const val API_URL = "http://refactoring.encs.concordia.ca/oracle/api.php"
private val REPOSITORY_URL_REGEX = Regex("""https://github.com/([\w\-]+)/([\w\-.]+)\.git""")

private fun getDataJsonAsText(): String {
    val file = File("$OUTPUTS_PATH/oracle/data.json")
    if (!file.exists()) {
        file.parentFile.mkdirs()
        FileUtils.copyURLToFile(URL("$API_URL?json"), file)
    }
    return file.readText()
}

private fun parseDataJson(json: String): List<RefOracleCommit> {
    // Fix wrong escape
    val fixed = json.replace("\\'", "'")
    return jacksonObjectMapper().readValue(fixed)
}

private fun getRefOracleCommits() = parseDataJson(getDataJsonAsText())

private fun convert(
    commits: List<RefOracleCommit>
): List<List<RefOracleData>> =
    commits.map { commit ->
        commit.refactorings.map {
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

fun getRefOracleDataList(
    type: String,
    size: Int,
    maxPerCommit: Int = 5,
    minPerCommit: Int = 1,
    maxSameType: Int = 1,
    overlap: Boolean = false,
    random: Boolean = false,
    validation: String = "TP",
    tools: List<String> = listOf("RefactoringMiner")
): List<RefOracleData> {
    val commits = getRefOracleCommits().map { commit ->
        RefOracleCommit(commit.id, commit.repository, commit.sha1,
            commit.refactorings.filter { ref ->
                ref.validation == validation && tools.all { tool -> ref.detectionTools.contains(tool) }
            }
        )
    }
    val refactoringLists = convert(commits)

    val candidateLists = refactoringLists.filter { it.size in minPerCommit..maxPerCommit }.map { refs ->
        refs.filter { it.type == type }
    }.filter { it.size in 1..maxSameType }

    // all refactorings are in different commits
    if (size <= candidateLists.size) {
        return (if (random) candidateLists.shuffled() else candidateLists).take(size).map { it.first() }
    }

    val candidates = candidateLists.flatten()
    if (overlap && size <= candidates.size) {
        return (if (random) candidates.shuffled() else candidates).take(size)
    }

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
