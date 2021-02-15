package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.koin.java.KoinJavaComponent.inject

object Refactorings : IntIdTable("refactorings") {
    val owner = reference("owner", Users)
    val commit = reference("commit", Commits)
    val type = reference("type", RefactoringTypes)
    val data = jsonb("data", ::stringify, ::parse)
    val description = text("description")
    val isVerified = bool("is_verified").default(false)
}

object RefactoringToRefactorings : Table("refactoring_to_refactorings") {
    val parent = reference("parent_refactoring_id", Refactorings)
    val child = reference("child_refactoring_id", Refactorings)
}

class RefactoringDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<Refactoring> {
    companion object : IntEntityClass<RefactoringDao>(Refactorings)

    var owner by UserDao referencedOn Refactorings.owner
    var parents by RefactoringDao.via(RefactoringToRefactorings.child, RefactoringToRefactorings.parent)
    var children by RefactoringDao.via(RefactoringToRefactorings.parent, RefactoringToRefactorings.child)
    var type by RefactoringTypeDao referencedOn Refactorings.type
    var commit by CommitDao referencedOn Refactorings.commit
    var data by Refactorings.data
    var description by Refactorings.description
    var isVerified by Refactorings.isVerified

    override fun asModel(): Refactoring {
        return Refactoring(
            this.id.value,
            this.owner.id.value,
            this.parents.singleOrNull()?.id?.value,
            this.commit.asModel(),
            this.type.name,
            this.data,
            this.description,
            this.isVerified
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
