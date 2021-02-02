package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.convert
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

const val OUTPUTS_PATH = "outputs"

fun main() {
    val types = listOf(
        "Extract Method",
        "Move Attribute",
        "Move Class",
        "Rename Variable"
    )

    val refactorings = types.map { RefactoringOracle.getRefactorings(it) }.flatten().withIndex()
    writeToCsv("refactorings.csv", refactorings)

    val experiment = types.map { type ->
        refactorings
            .filter { it.value.type == type }
            .filter { it.value.others.size in 2..4 }
            .shuffled().take(4).sortedBy { it.index }
    }.flatten()

    writeToCsv("experiment.csv", experiment)
    writeToNdJson("type.ndjson", experiment, withDescription = false)
    writeToNdJson("description.ndjson", experiment, withDescription = true)
    writeToNdJson("refminer.ndjson", experiment, withReDetection = true)
}

private fun getOutputFile(name: String): File {
    val file = File("$OUTPUTS_PATH/$name")
    file.parentFile.mkdirs()
    if (!file.exists()) file.createNewFile()
    return file
}

private fun writeToCsv(name: String, refactorings: Iterable<IndexedValue<RefactoringOracle.Refactoring>>) {
    val csv = getOutputFile(name)
    BufferedWriter(FileWriter(csv, true)).use { out ->
        out.appendln(
            listOf(
                "ID",
                "Type",
                "Description",
                "Commit",
                "Size/Commit",
                "Others"
            ).joinToString("\",\"", "\"", "\"")
        )
        for ((i, refactoring) in refactorings) {
            out.appendln(
                listOf(
                    "$i",
                    refactoring.type,
                    refactoring.description.replace("\"", "\"\""),
                    "https://github.com/${refactoring.commit.owner}/${refactoring.commit.repository}/commit/${refactoring.commit.sha}",
                    refactoring.others.size,
                    refactoring.others.joinToString("\n") { it.description.replace("\"", "\"\"") }
                ).joinToString("\",\"", "\"", "\"")
            )
        }
    }
}

private fun writeToNdJson(
    name: String,
    refactorings: Iterable<IndexedValue<RefactoringOracle.Refactoring>>,
    withDescription: Boolean = false,
    withReDetection: Boolean = false
) {
    val json = getOutputFile(name)

    for ((_, refactoring) in refactorings) {
        if (withReDetection) {
            try {
                RefactoringMiner.reDetect(refactoring) {
                    BufferedWriter(FileWriter(json, true)).use { out ->
                        out.appendln(
                            jacksonObjectMapper().writeValueAsString(
                                convert(it, refactoring)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            BufferedWriter(FileWriter(json, true)).use { out ->
                out.appendln(
                    jacksonObjectMapper().writeValueAsString(
                        Refactoring(
                            refactoring.type,
                            refactoring.commit,
                            description = if (withDescription) refactoring.description else ""
                        )
                    )
                )
            }
        }
    }
}
