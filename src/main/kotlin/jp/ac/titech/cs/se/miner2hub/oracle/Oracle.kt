package jp.ac.titech.cs.se.miner2hub.oracle

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

object Oracle {
    private fun getDataJson(): String? = javaClass.classLoader.getResource("rminer/data.json")?.readText()

    private fun parseDataJson(json: String) = jacksonObjectMapper().readValue<List<Commit>>(json)

    private fun convert(commits: List<Commit>) = commits.map { commit ->
        commit.refactorings.filter {
            it.validation == "TP" && it.detectionTools.contains("RefactoringMiner")
        }.map {
            RefactoringMetadata(
                it.type,
                it.description,
                commit.repository,
                commit.sha1
            )
        }
    }

    fun getDataset(type: String, n: Int, m: Int = 10, random: Boolean = false): List<RefactoringMetadata> {
        val json = getDataJson() ?: throw IOException("data.json is not found.")
        val commits = parseDataJson(json)
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

        println("number of type='${type}' is ${candidates.size}")
        return candidates
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Commit(
    val id: Int,
    val repository: String,
    val sha1: String,
    val refactorings: List<Refactoring>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Refactoring(
        val type: String,
        val description: String,
        val validation: String,
        val detectionTools: String
    )
}
