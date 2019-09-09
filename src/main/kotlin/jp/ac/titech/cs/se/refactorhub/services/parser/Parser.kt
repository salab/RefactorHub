package jp.ac.titech.cs.se.refactorhub.services.parser

import jp.ac.titech.cs.se.refactorhub.models.element.Element

interface Parser {
    fun parse(text: String): List<Element>

    companion object {
        fun get(extension: String): Parser = when (extension) {
            else -> object : Parser {
                override fun parse(text: String) = emptyList<Element>()
            }
        }
    }
}