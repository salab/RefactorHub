package jp.ac.titech.cs.se.refactorhub.app.model

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.Token
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

data class File(
    val path: String = "",
    override val text: String = "",
    override val extension: String = "",
    override val elements: List<CodeElement> = listOf(),
    override val tokens: List<Token> = listOf()
) : File

data class ChangedFile(
    val path: String = "",
    val beforePath: String = "",
    val status: ChangedFileStatus,
    override val text: String = "",
    override val extension: String = "",
    override val elements: List<CodeElement> = listOf(),
    override val tokens: List<Token> = listOf()
) : File

@Suppress("EnumEntryName")
enum class ChangedFileStatus {
    modified, added, removed, renamed
}
