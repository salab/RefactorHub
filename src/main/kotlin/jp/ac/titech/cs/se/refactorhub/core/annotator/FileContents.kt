package jp.ac.titech.cs.se.refactorhub.core.annotator

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.getDiffHunks
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileContent
import jp.ac.titech.cs.se.refactorhub.core.parser.CodeParser
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

val GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN") ?: ""


fun main() {
    val owner = "junit-team"
    val repository = "junit5"
    val sha = "46b63f50653dce87d451d0470f43d1b17752b770"
    val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)

    val detail = client.getRepository("$owner/$repository").getCommit(sha).let { commit ->
        CommitDetail(
            commit.owner.ownerName,
            commit.owner.name,
            commit.shA1,
            commit.htmlUrl.toExternalForm(),
            commit.commitShortInfo.message,
            commit.commitShortInfo.author.name,
            commit.commitShortInfo.authoredDate,
            commit.files.map { file ->
                val patch = file.patch ?: ""
                val diffHunks = getDiffHunks(patch)
                val linesDeleted = diffHunks.fold(0) { acc, (before) -> acc + (before?.let { it.endLine - it.startLine + 1 } ?: 0)}
                val linesAdded = diffHunks.fold(0) { acc, (after) -> acc + (after?.let { it.endLine - it.startLine + 1 } ?: 0)}
                assert(file.linesDeleted == linesDeleted && file.linesAdded == linesAdded)
                CommitFile(
                    file.sha,
                    CommitFileStatus.valueOf(file.status),
                    file.fileName,
                    file.previousFilename ?: file.fileName,
                    patch,
                    diffHunks
                )
            },
            commit.parentSHA1s.firstOrNull() ?: throw BadRequestException("First commit is not supported")
        )
    }
    val commit = Commit(owner, repository, sha)
    val content = CommitContent(
        commit,
        detail.files.map {
            CommitContent.FilePair(
                CommitContent.File(
                    it.previousName,
                    getFileContent(
                        commit.owner,
                        commit.repository,
                        detail.parent,
                        it.previousName
                    )
                ),
                CommitContent.File(
                    it.name,
                    getFileContent(
                        commit.owner,
                        commit.repository,
                        detail.sha,
                        it.name
                    )
                ),
                it.patch,
                it.diffHunks
            )
        }
    )

    content.files.forEach { file ->
        file.before.content.tokens.filter { !it.isSymbol }.forEach { println(it) }
        file.after.content.tokens.filter { !it.isSymbol }.forEach { println(it) }
    }
}

fun getFileContent(owner: String, repository: String, sha: String, path: String): FileContent {
    return try {
        createFileContent(getGHContent(owner, repository, sha, path))
    } catch (e: IOException) {
        FileContent(e.localizedMessage, uri = "https://github.com/$owner/$repository/blob/$sha/$path")
    }
}

private fun getGHContent(
    owner: String,
    repository: String,
    sha: String,
    path: String
): GHContent {
    val client = GitHub.connectUsingOAuth(GITHUB_ACCESS_TOKEN)
    return client.getRepository("$owner/$repository").getFileContent(path, sha)
}

private fun createFileContent(content: GHContent): FileContent {
    val text = if (content.isText) IOUtils.toString(content.read())
    else return FileContent("This is a binary file.", uri = content.htmlUrl)
    val extension = FilenameUtils.getExtension(content.name)
    return FileContent(
        text,
        extension,
        content.htmlUrl,
        CodeParser.get(extension).parse(text, content.path),
        CodeParser.get(extension).tokenize(text)
    )
}

private val GHContent.isText: Boolean
    get() = (URL(downloadUrl).openConnection() as HttpURLConnection).run {
        requestMethod = "HEAD"
        connect()
        contentType.matches(Regex("text/.*"))
    }
