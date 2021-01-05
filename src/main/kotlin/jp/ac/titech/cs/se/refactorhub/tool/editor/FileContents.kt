package jp.ac.titech.cs.se.refactorhub.tool.editor

import jp.ac.titech.cs.se.refactorhub.tool.model.editor.FileContent
import jp.ac.titech.cs.se.refactorhub.tool.parser.CodeElementParser
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub
import java.net.HttpURLConnection
import java.net.URL

val GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN") ?: ""

fun getFileContent(sha: String, owner: String, repository: String, path: String): FileContent {
    return createFileContent(getGHContent(sha, owner, repository, path))
}

private fun getGHContent(
    sha: String,
    owner: String,
    repository: String,
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
        CodeElementParser.get(extension).parse(text, content.path)
    )
}

private val GHContent.isText: Boolean
    get() = (URL(downloadUrl).openConnection() as HttpURLConnection).run {
        requestMethod = "HEAD"
        connect()
        contentType.matches(Regex("text/.*"))
    }
