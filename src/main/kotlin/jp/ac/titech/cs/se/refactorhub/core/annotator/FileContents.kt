package jp.ac.titech.cs.se.refactorhub.core.annotator

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
