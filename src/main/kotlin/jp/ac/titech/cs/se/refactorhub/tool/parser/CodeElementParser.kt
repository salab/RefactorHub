package jp.ac.titech.cs.se.refactorhub.tool.parser

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.parser.impl.java.JavaParser

interface CodeElementParser {
    fun parse(text: String, path: String): List<CodeElement>

    companion object {
        fun get(extension: String): CodeElementParser = when (extension) {
            "java" -> JavaParser()
            else -> object : CodeElementParser {
                override fun parse(text: String, path: String) = emptyList<CodeElement>()
            }
        }
    }
}
