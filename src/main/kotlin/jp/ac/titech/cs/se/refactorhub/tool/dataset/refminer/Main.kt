package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.convert
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.miner.reDetect
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.getRefOracleDataset
import java.io.File
import java.io.FileWriter

const val OUTPUTS_PATH = "outputs"
const val DATASET_PATH = "$OUTPUTS_PATH/dataset.ndjson"

fun main() {
    createDataset("Extract Method", 5, 1, false)
    createDataset("Rename Method", 5, 3, false)
    createDataset("Move Method", 5, 1, false)
    createDataset("Rename Variable", 5, 3, false)
    createDataset("Parameterize Variable", 5, 3, false)
    createDataset("Extract And Move Method", 5, 5, false)
}

fun createDataset(type: String, n: Int, m: Int, random: Boolean) {
    val file = File(DATASET_PATH)
    if (!file.exists()) file.createNewFile()
    val dataset = getRefOracleDataset(type, n * 2, m, random)
    var count = 0
    for (data in dataset) {
        if (count >= n) break
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
