package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringDraft
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent.inject

object RefactoringDrafts : IntIdTable("refactoring_drafts") {
    val owner = reference("owner", Users)
    val origin = reference("origin", Refactorings)
    val isFork = bool("is_fork")
    val commit = reference("commit", Commits)
    val type = reference("type", RefactoringTypes)
    val data = jsonb("data", ::stringify, ::parse)
    val description = text("description")
}

class RefactoringDraftDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<RefactoringDraft> {
    companion object : IntEntityClass<RefactoringDraftDao>(RefactoringDrafts)

    var owner by UserDao referencedOn RefactoringDrafts.owner
    var origin by RefactoringDao referencedOn RefactoringDrafts.origin
    var isFork by RefactoringDrafts.isFork
    var commit by CommitDao referencedOn RefactoringDrafts.commit
    var type by RefactoringTypeDao referencedOn RefactoringDrafts.type
    var data by RefactoringDrafts.data
    var description by Refactorings.description

    override fun asModel(): RefactoringDraft {
        return RefactoringDraft(
            this.id.value,
            this.owner.id.value,
            this.origin.id.value,
            this.isFork,
            this.commit.asModel(),
            this.type.name,
            this.data,
            this.description
        )
    }
}

private fun stringify(data: Refactoring.Data): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}

private fun parse(src: String): Refactoring.Data {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
