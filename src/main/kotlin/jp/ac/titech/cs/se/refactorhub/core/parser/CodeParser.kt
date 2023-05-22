package jp.ac.titech.cs.se.refactorhub.core.parser

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.Token
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.parser.impl.java.JavaParser

interface CodeParser {
    fun parse(text: String, path: String): List<CodeElement>

    fun tokenize(text: String): List<Token>

    companion object {
        fun get(extension: String): CodeParser = when (extension) {
            "java" -> JavaParser()
            else -> object : CodeParser {
                override fun parse(text: String, path: String) = emptyList<CodeElement>()
                override fun tokenize(text: String) = emptyList<Token>()
            }
        }
    }
}
