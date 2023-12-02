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

/**
 * example:
 * ```
 * @@ -0,0 +1,3 @@
 * +class Add {
 * +    String text = "this file was added";
 * +}
 * ```
 * will be
 * ```
 * DiffHunk(
 *   before=null,
 *   after=Hunk(startLine=1, endLine=3, oppositeLine=0)
 * )
 * ```
 */
data class DiffHunk(
    val before: Hunk?,
    val after: Hunk?
) {
    data class Hunk(
        val startLine: Int,
        val endLine: Int,
        /**
         * If this `hunk` is in before, the previous line number in after.
         * Otherwise, the previous line number in before.
         * This value can be 0.
         */
        val oppositeLine: Int
    )
}
