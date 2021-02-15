package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent.inject

object CommitContents : IntIdTable("commit_contents") {
    val commit = reference("commit", Commits)
    val files = jsonb("files", ::stringify, ::parse)
}

class CommitContentDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<CommitContent> {
    companion object : IntEntityClass<CommitContentDao>(CommitContents)

    var commit by CommitDao referencedOn CommitContents.commit
    var files by CommitContents.files

    override fun asModel(): CommitContent {
        return CommitContent(
            this.commit.asModel(),
            this.files
        )
    }
}

private fun stringify(files: List<CommitContent.FilePair>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(files)
}

private fun parse(src: String): List<CommitContent.FilePair> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
