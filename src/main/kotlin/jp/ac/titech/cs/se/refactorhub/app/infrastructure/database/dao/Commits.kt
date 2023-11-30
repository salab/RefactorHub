package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Commit
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.koin.java.KoinJavaComponent
import java.time.LocalDateTime
import java.util.UUID

object Commits : UUIDTable("commits") {
    val owner = varchar("owner", 100)
    val repository = varchar("repository", 100)
    val sha = varchar("sha", 40)
    val parentSha = varchar("parent_sha", 40)
    val url = varchar("url", 300)
    val message = text("message")
    val authorName = varchar("author_name", 100)
    val authoredDateTime = varchar("authored_date_time", 50)
    val beforeFiles = jsonb("before_files", ::stringifyFileList, ::parseFileList)
    val afterFiles = jsonb("after_files", ::stringifyFileList, ::parseFileList)
    val fileMappings = jsonb("file_mappings", ::stringifyFileMappings, ::parseFileMappings)
    val patch = text("patch")
}

class CommitDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Commit> {
    companion object : UUIDEntityClass<CommitDao>(Commits)

    var owner by Commits.owner
    var repository by Commits.repository
    var sha by Commits.sha
    var parentSha by Commits.parentSha
    var url by Commits.url
    var message by Commits.message
    var authorName by Commits.authorName
    var authoredDateTime by Commits.authoredDateTime
    var beforeFiles by Commits.beforeFiles
    var afterFiles by Commits.afterFiles
    var fileMappings by Commits.fileMappings
    var patch by Commits.patch

    override fun asModel(): Commit {
        return Commit(
            this.id.value,
            this.owner,
            this.repository,
            this.sha,
            this.parentSha,
            this.url,
            this.message,
            this.authorName,
            LocalDateTime.parse(this.authoredDateTime),
            this.beforeFiles,
            this.afterFiles,
            this.fileMappings,
            this.patch
        )
    }
}

private fun stringifyFileList(data: List<File>): String {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseFileList(src: String): List<File> {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}

private fun stringifyFileMappings(data: List<FileMapping>): String {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseFileMappings(src: String): List<FileMapping> {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
