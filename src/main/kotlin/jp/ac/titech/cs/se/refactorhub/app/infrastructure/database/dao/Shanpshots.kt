package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.ChangedFile
import jp.ac.titech.cs.se.refactorhub.app.model.Snapshot
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.DiffHunk
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.koin.java.KoinJavaComponent.inject
import java.util.UUID

object Snapshots : UUIDTable("snapshots") {
    val files = jsonb("files", ::stringifyChangedFileList, ::parseChangedFileList)
    val diffHunks = jsonb("diffHunks", ::stringifyDiffHunks, ::parseDiffHunks)
    val patch = text("patch")
}

object SnapshotToChanges : Table("snapshot_to_changes") {
    val snapshot = reference("snapshot", Snapshots)
    val change = reference("change", Changes)
    override val primaryKey = PrimaryKey(snapshot, change)
}

class SnapshotDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Snapshot> {
    companion object : UUIDEntityClass<SnapshotDao>(Snapshots)

    var files by Snapshots.files
    var diffHunks by Snapshots.diffHunks
    var patch by Snapshots.patch
    var changes by ChangeDao via SnapshotToChanges

    override fun asModel() = Snapshot(
        this.id.value,
        this.files,
        this.diffHunks,
        this.patch,
        this.changes.map { it.asModel() }
    )
}

private fun stringifyChangedFileList(data: List<ChangedFile>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseChangedFileList(src: String): List<ChangedFile> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}

private fun stringifyDiffHunks(data: List<DiffHunk>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseDiffHunks(src: String): List<DiffHunk> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
