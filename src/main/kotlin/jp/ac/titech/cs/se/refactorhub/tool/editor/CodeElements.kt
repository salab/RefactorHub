package jp.ac.titech.cs.se.refactorhub.tool.editor

import jp.ac.titech.cs.se.refactorhub.tool.model.editor.FileContent
import jp.ac.titech.cs.se.refactorhub.tool.parser.CodeElementParser
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub
import java.net.HttpURLConnection
import java.net.URL

fun getFileContent(sha: String, owner: String, repository: String, path: String, token: String? = null): FileContent {
    return createFileContent(getGHContent(sha, owner, repository, path, token))
}

private fun getGHContent(
    sha: String,
    owner: String,
    repository: String,
    path: String,
    token: String? = null
): GHContent {
    val client = GitHub.connectUsingOAuth(token)
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
