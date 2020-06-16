package jp.ac.titech.cs.se.miner2hub

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.miner2hub.converter.convert
import jp.ac.titech.cs.se.miner2hub.miner.Miner
import jp.ac.titech.cs.se.miner2hub.oracle.Oracle

fun main(args: Array<String>) {
    val dataset = Oracle.getDataset("Extract Method", 1, 1, false)
    dataset.forEach { metadata ->
        Miner.reDetect(metadata) {
            println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(convert(it, metadata)))
        }
    }
}
