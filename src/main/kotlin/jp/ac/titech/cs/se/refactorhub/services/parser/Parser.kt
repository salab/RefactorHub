package jp.ac.titech.cs.se.refactorhub.services.parser

import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.services.parser.impl.java.JavaParser

interface Parser {
    fun parse(text: String, path: String): List<Element>

    companion object {
        fun get(extension: String): Parser = when (extension) {
            "java" -> JavaParser()
            else -> object : Parser {
                override fun parse(text: String, path: String) = emptyList<Element>()
            }
        }
    }
}
