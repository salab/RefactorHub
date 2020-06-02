package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MapKeyColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "refactoring_type")
data class RefactoringType(
    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    @ElementCollection
    @Fetch(FetchMode.JOIN)
    @MapKeyColumn(name = "before_key", length = 100)
    @Column(name = "before_element", nullable = false, columnDefinition = "blob")
    val before: Map<String, Element.Info> = mapOf(),

    @ElementCollection
    @Fetch(FetchMode.JOIN)
    @MapKeyColumn(name = "after_key", length = 100)
    @Column(name = "after_element", nullable = false, columnDefinition = "blob")
    val after: Map<String, Element.Info> = mapOf(),

    @Column(name = "description", nullable = false, columnDefinition = "text")
    var description: String = "",

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    val id: Long = 0
) {
    @JsonIgnore
    @OneToMany(mappedBy = "type")
    @Column(name = "refactorings", nullable = false)
    val refactorings: MutableList<Refactoring> = ArrayList()

    @JsonIgnore
    @OneToMany(mappedBy = "type")
    @Column(name = "drafts", nullable = false)
    val drafts: MutableList<Draft> = ArrayList()
}
