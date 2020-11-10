package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.convert
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.miner.reDetect
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.getRefOracleDataList
import java.io.File
import java.io.FileWriter

const val OUTPUTS_PATH = "outputs"

fun main() {
    val types = listOf(
        "Extract Method",
        "Rename Method",
        "Move Method",
        "Extract Variable",
        "Rename Parameter",
        "Move Class"
    )
    // createTypeDataset(types, 5, 5)
    // createDescriptionDataset(types, 5, 5)
    // createRefactoringMinerDataset(types, 5, 5)
}

fun getOutputFile(name: String): File {
    val file = File("$OUTPUTS_PATH/$name.ndjson")
    file.parentFile.mkdirs()
    if (!file.exists()) file.createNewFile()
    return file
}

fun createTypeDataset(types: List<String>, size: Int, maxPerCommit: Int) {
    val file = getOutputFile("type")

    for (type in types) {
        val dataList = getRefOracleDataList(type, size, maxPerCommit)
        for (data in dataList) {
            FileWriter(file, true).use { out ->
                out.appendln(
                    jacksonObjectMapper().writeValueAsString(
                        Refactoring(data.type, data.commit)
                    )
                )
            }
        }
    }
}

fun createDescriptionDataset(types: List<String>, size: Int, maxPerCommit: Int) {
    val file = getOutputFile("description")

    for (type in types) {
        val dataList = getRefOracleDataList(type, size, maxPerCommit)
        for (data in dataList) {
            FileWriter(file, true).use { out ->
                out.appendln(
                    jacksonObjectMapper().writeValueAsString(
                        Refactoring(data.type, data.commit, description = data.description)
                    )
                )
            }
        }
    }
}

fun createRefactoringMinerDataset(types: List<String>, size: Int, maxPerCommit: Int) {
    val file = getOutputFile("refminer")

    for (type in types) {
        val dataList = getRefOracleDataList(type, Int.MAX_VALUE, maxPerCommit)
        var count = 0
        for (data in dataList) {
            if (count >= size) break
            try {
                reDetect(data) {
                    FileWriter(file, true).use { out ->
                        out.appendln(
                            jacksonObjectMapper().writeValueAsString(
                                convert(
                                    it,
                                    data
                                )
                            )
                        )
                    }
                }
                count++
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
