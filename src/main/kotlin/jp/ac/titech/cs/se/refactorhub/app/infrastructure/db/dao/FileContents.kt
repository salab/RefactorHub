package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent.inject

object FileContents : IntIdTable("file_contents") {
    val commit = reference("commit", Commits)
    val files = jsonb("files", ::stringify, ::parse)
}

class FileContentDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<CommitFileContents> {
    companion object : IntEntityClass<FileContentDao>(FileContents)

    var commit by CommitDao referencedOn FileContents.commit
    var files by FileContents.files

    override fun asModel(): CommitFileContents {
        return CommitFileContents(
            this.commit.asModel(),
            this.files
        )
    }
}

private fun stringify(files: CommitFileContents.Files): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(files)
}

private fun parse(src: String): CommitFileContents.Files {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
