package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import javax.persistence.*

@Entity
@Table(name = "refactoring_type")
data class RefactoringType(
    @Column(name = "name", nullable = false, unique = true)
    var name: String = "",

    @ElementCollection
    @Column(name = "before", nullable = false, columnDefinition = "text")
    val before: Map<String, Element.Type> = mapOf(),

    @ElementCollection
    @Column(name = "after", nullable = false, columnDefinition = "text")
    val after: Map<String, Element.Type> = mapOf(),

    @Column(name = "description", nullable = false)
    var description: String = "",

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    val id: Long = 0
) {
    @JsonIgnore
    @OneToMany(mappedBy = "type")
    @Column(name = "refactorings", nullable = false)
    val refactorings: MutableSet<Refactoring> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "type")
    @Column(name = "drafts", nullable = false)
    val drafts: MutableSet<Draft> = mutableSetOf()
}
