package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.core.dataset.OUTPUTS_PATH
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Commit
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

object RefactoringOracle {
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

    private fun parseDataJson(json: String): List<RefactoringCommit> {
        // Fix wrong escape
        val fixed = json.replace("\\'", "'")
        return jacksonObjectMapper().readValue(fixed)
    }

    private fun getRefactoringCommits() = parseDataJson(getDataJsonAsText())

    fun getRefactorings(
        type: String,
        size: Int = Int.MAX_VALUE,
        sizePerCommit: IntRange = 1..5,
        random: Boolean = false,
        validation: String = "TP",
        tools: List<String> = listOf("RefactoringMiner")
    ): List<Refactoring> {
        val commits = getRefactoringCommits().map {
            RefactoringCommit(
                it.id,
                it.repository,
                it.sha1,
                it.refactorings.filter { ref ->
                    ref.validation == validation && tools.all { tool -> ref.detectionTools.contains(tool) }
                }
            )
        }

        val candidates = commits.filter {
            it.refactorings.size in sizePerCommit && it.refactorings.count { ref -> ref.type == type } == 1
        }.map {
            val refactoring = it.refactorings.first { ref -> ref.type == type }
            Refactoring(
                refactoring.type,
                refactoring.description,
                it.commit,
                it.refactorings.map { ref ->
                    Refactoring(ref.type, ref.description, it.commit)
                }
            )
        }

        if (size <= candidates.size) {
            return (if (random) candidates.shuffled() else candidates).take(size)
        }
        return (if (random) candidates.shuffled() else candidates)
    }

    data class Refactoring(
        val type: String,
        val description: String,
        val commit: Commit,
        val others: List<Refactoring> = listOf()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RefactoringCommit(
        val id: Int,
        val repository: String,
        val sha1: String,
        val refactorings: List<Refactoring>
    ) {
        val commit: Commit by lazy {
            val result = REPOSITORY_URL_REGEX.matchEntire(this.repository)
                ?: throw RuntimeException("repository url is invalid: ${this.repository}")
            val (owner, repository) = result.destructured
            Commit(this.sha1, owner, repository)
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Refactoring(
            val type: String,
            val description: String,
            val validation: String,
            val detectionTools: String
        )
    }
}
