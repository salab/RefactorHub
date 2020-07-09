package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent

object RefactoringDrafts : IntIdTable("refactoring_drafts") {
    val owner = reference("owner", Users)
    val commit = reference("commit", Commits)
    val origin = reference("origin", Refactorings).nullable()
    val type = reference("type", RefactoringTypes)
    val data = jsonb("data", ::stringify, ::parse)
    val description = text("description")
}

class RefactoringDraftDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RefactoringDraftDao>(RefactoringDrafts)

    var owner by UserDao referencedOn RefactoringDrafts.owner
    var commit by CommitDao referencedOn RefactoringDrafts.commit
    var origin by RefactoringDao optionalReferencedOn RefactoringDrafts.origin
    var type by RefactoringTypeDao referencedOn RefactoringDrafts.type
    var data by RefactoringDrafts.data
    var description by Refactorings.description
}

private fun stringify(data: Refactoring.Data): String {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}

private fun parse(src: String): Refactoring.Data {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
