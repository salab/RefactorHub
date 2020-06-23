package jp.ac.titech.cs.se.miner2hub

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.miner2hub.converter.convert
import jp.ac.titech.cs.se.miner2hub.miner.Miner
import jp.ac.titech.cs.se.miner2hub.oracle.Oracle
import java.io.File
import java.io.FileWriter

const val PATH = "outputs/dataset.ndjson"

fun main(args: Array<String>) {
    val file = File(PATH)
    if (!file.exists()) file.createNewFile()
    val dataset = Oracle.getDataset("Rename Method", 7, 3, false)
    dataset.forEach { metadata ->
        try {
            Miner.reDetect(metadata) {
                FileWriter(file, true).use { out ->
                    out.appendln(jacksonObjectMapper().writeValueAsString(convert(it, metadata)))
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}
