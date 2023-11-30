package jp.ac.titech.cs.se.refactorhub.core.model.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

data class File(
    val path: String = "",
    val text: String = "",
    val extension: String = "",
    val elements: List<CodeElement> = listOf(),
    val tokens: List<Token> = listOf()
)

data class Token(
    val raw: String,
    val start: Int,
    val end: Int,
    val isSymbol: Boolean, // e.g. ";", "(", "+=", "."
    val isComment: Boolean
)

data class FileMapping(
    val status: FileMappingStatus,
    val beforePath: String?,
    val afterPath: String?,
    val diffHunks: List<DiffHunk>
)

@Suppress("EnumEntryName")
enum class FileMappingStatus {
    modified, added, removed, renamed, unmodified
}

data class DiffHunk(
    val before: Hunk?,
    val after: Hunk?
) {
    data class Hunk(
        val startLine: Int,
        val endLine: Int
    )
}
