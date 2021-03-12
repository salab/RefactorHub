package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.RefactoringType
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.koin.java.KoinJavaComponent.inject

object RefactoringTypes : IntIdTable("refactoring_types") {
    val owner = reference("owner", Users)
    val name = varchar("name", 100).uniqueIndex()
    val before = jsonb("before", ::stringify, ::parse)
    val after = jsonb("after", ::stringify, ::parse)
    val description = text("description")
    val url = varchar("url", 500)
}

class RefactoringTypeDao(id: EntityID<Int>) : IntEntity(id), ModelConverter<RefactoringType> {
    companion object : IntEntityClass<RefactoringTypeDao>(RefactoringTypes)

    var owner by UserDao referencedOn RefactoringTypes.owner
    var name by RefactoringTypes.name
    var before by RefactoringTypes.before
    var after by RefactoringTypes.after
    var description by RefactoringTypes.description
    var url by RefactoringTypes.url

    override fun asModel(): RefactoringType {
        return RefactoringType(
            this.id.value,
            this.owner.id.value,
            this.name,
            this.before,
            this.after,
            this.description,
            this.url
        )
    }
}

private fun stringify(data: Map<String, CodeElementMetadata>): String {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}

private fun parse(src: String): Map<String, CodeElementMetadata> {
    val mapper by inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
