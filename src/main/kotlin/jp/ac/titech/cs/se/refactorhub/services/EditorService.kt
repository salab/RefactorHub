package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.models.editor.FileContent
import jp.ac.titech.cs.se.refactorhub.services.parser.Parser
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHContent
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class EditorService {

    fun createFileContent(content: GHContent): FileContent {
        val text = if (content.isText) IOUtils.toString(content.read(), Charsets.UTF_8.name())
        else return FileContent("This is a binary file.", uri = content.htmlUrl)
        val extension = FilenameUtils.getExtension(content.name)
        return FileContent(
            text,
            extension,
            content.htmlUrl,
            Parser.get(extension).parse(text, content.path)
        )
    }

    val GHContent.isText: Boolean
        get() = (URL(downloadUrl).openConnection() as HttpURLConnection).run {
            requestMethod = "HEAD"
            connect()
            contentType.matches(Regex("text/.*"))
        }

}