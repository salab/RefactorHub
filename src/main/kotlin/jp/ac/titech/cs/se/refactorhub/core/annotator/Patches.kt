package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMappingStatus
import java.net.HttpURLConnection
import java.net.URI

fun mapFiles(beforeFiles: List<File>?, owner: String, repository: String, sha: String): List<FileMapping> {
    return mapFiles(beforeFiles, fetchPatchFromGitHub(owner, repository, sha))
}
fun mapFiles(beforeFiles: List<File>?, patch: String): List<FileMapping> {
    val fileMappings = parsePatch(patch).toMutableList()

    beforeFiles
        ?.filter { beforeFile -> fileMappings.none { it.beforePath == beforeFile.path } }
        ?.forEach { fileMappings.add(FileMapping(FileMappingStatus.unmodified, it.path, it.path, listOf())) }

    return fileMappings
}

fun fetchPatchFromGitHub(owner: String, repository: String, sha: String): String {
    val url = URI("https://github.com/$owner/$repository/commit/$sha.diff").toURL()
    return (url.openConnection() as HttpURLConnection).run {
        connect()
        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

private fun parsePatch(patch: String): List<FileMapping> {
    val fileMappings = listOf<FileMapping>().toMutableList()
    if (!patch.startsWith("diff --git")) return fileMappings

    // document of format: https://git-scm.com/docs/diff-format#generate_patch_text_with_p
    val diffs = listOf<String>().toMutableList()
    val temporaryDiff = StringBuilder()
    for (raw in patch.split("\n").map { it + "\n" }) {
        if (raw.startsWith("diff --git") && temporaryDiff.isNotEmpty()) {
            diffs.add(temporaryDiff.toString())
            temporaryDiff.clear()
        }
        temporaryDiff.append(raw)
    }
    if (temporaryDiff.isNotEmpty()) diffs.add(temporaryDiff.toString())

    for (diff in diffs) {
        var status = FileMappingStatus.modified
        var beforePath: String? = null
        var afterPath: String? = null
        var isHeaderRaw = true
        val diffBody = StringBuilder()
        for (raw in diff.split("\n").map { it + "\n" }) {
            if (isHeaderRaw) {
                if (raw.startsWith("new file mode")) status = FileMappingStatus.added
                else if (raw.startsWith("deleted file mode")) status = FileMappingStatus.removed
                else if (raw.startsWith("rename from ")) {
                    status = FileMappingStatus.renamed
                    beforePath = raw.substring("rename from ".length).substringBefore("\n")
                } else if (raw.startsWith("rename to ")) {
                    status = FileMappingStatus.renamed
                    afterPath = raw.substring("rename to ".length).substringBefore("\n")
                } else if (raw.startsWith("--- a/")) beforePath = raw.substring("--- a/".length).substringBefore("\n")
                else if (raw.startsWith("+++ b/")) afterPath = raw.substring("+++ b/".length).substringBefore("\n")
                else if (raw.startsWith("@@")) {
                    isHeaderRaw = false
                    diffBody.append(raw)
                }
            } else {
                diffBody.append(raw)
            }
        }
        val diffHunks = parseDiffHunk(diffBody.toString())
        fileMappings.add(FileMapping(status, beforePath, afterPath, diffHunks))
    }

    return fileMappings
}

private fun parseDiffHunk(diffBody: String): List<DiffHunk> {
    val diffHunks = ArrayList<DiffHunk>()
    val before = object { var currentLine = 0; var hunkStartLine = 0; var hunkEndLine = 0; var isInHunk = false }
    val after = object { var currentLine = 0; var hunkStartLine = 0; var hunkEndLine = 0; var isInHunk = false }
    fun addDiffHunk() {
        var beforeHunk: DiffHunk.Hunk? = null
        var afterHunk: DiffHunk.Hunk? = null
        if (before.isInHunk) beforeHunk = DiffHunk.Hunk(before.hunkStartLine, before.hunkEndLine,
            if (after.isInHunk) after.hunkStartLine - 1 else after.currentLine)
        if (after.isInHunk) afterHunk = DiffHunk.Hunk(after.hunkStartLine, after.hunkEndLine,
            if (before.isInHunk) before.hunkStartLine - 1 else before.currentLine)
        diffHunks.add(DiffHunk(beforeHunk, afterHunk))
    }
    fun updateEndLine(diffCategory: DiffCategory) {
        if (diffCategory == DiffCategory.before) before.hunkEndLine = before.currentLine
        else after.hunkEndLine = after.currentLine
    }

    val diffBodyRaws = diffBody.split("\n").map { it + "\n" }
    val headerRegex = Regex("""^@@ -(\d+),\d+ \+(\d+),\d+ @@""")
    for (raw in diffBodyRaws) {
        val matchedHeader = headerRegex.find(raw)
        if (matchedHeader != null) {
            // header
            if (before.isInHunk) updateEndLine(DiffCategory.before)
            if (after.isInHunk) updateEndLine(DiffCategory.after)
            if (before.isInHunk || after.isInHunk) {
                addDiffHunk()
                before.isInHunk = false
                after.isInHunk = false
            }

            assert(matchedHeader.groups.size == 3)
            // matchedHeader.groups[0] holds the entire matched string e.g. "@@ -(g1),1 +(g2),1 @@"
            before.currentLine = matchedHeader.groups[1]!!.value.toInt()
            after.currentLine = matchedHeader.groups[2]!!.value.toInt()
            // NOTE: the line of next raw will be matchedHeader.groups[1]
            if (before.currentLine != 0) before.currentLine -= 1
            if (after.currentLine != 0) after.currentLine -= 1
            continue
        }
        if (raw.startsWith("-")) {
            // deleted line
            before.currentLine++
            if (!before.isInHunk) {
                // the start of hunk
                before.hunkStartLine = before.currentLine
                before.isInHunk = true
                if (after.isInHunk) updateEndLine(DiffCategory.after)
            }
            continue
        }
        if (raw.startsWith("+")) {
            // added line
            after.currentLine++
            if (!after.isInHunk) {
                // the start of hunk
                after.hunkStartLine = after.currentLine
                after.isInHunk = true
                if (before.isInHunk) updateEndLine(DiffCategory.before)
            }
            continue
        }
        // common line
        if (before.isInHunk) updateEndLine(DiffCategory.before)
        if (after.isInHunk) updateEndLine(DiffCategory.after)
        if (before.isInHunk || after.isInHunk) {
            addDiffHunk()
            before.isInHunk = false
            after.isInHunk = false
        }
        before.currentLine++
        after.currentLine++
    }

    if (before.isInHunk) updateEndLine(DiffCategory.before)
    if (after.isInHunk) updateEndLine(DiffCategory.after)
    if (before.isInHunk || after.isInHunk) addDiffHunk()
    return diffHunks
}
