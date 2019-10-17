package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import java.io.Serializable
import java.util.*
import javax.persistence.*
import kotlin.reflect.full.createInstance

@Entity
@Table(name = "refactoring")
data class Refactoring(
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    val owner: User,

    @ManyToOne
    @JoinColumn(name = "commit", nullable = false)
    val commit: Commit,

    @ManyToOne
    @JoinColumn(name = "parent", nullable = true)
    var parent: Refactoring? = null,

    @ManyToOne
    @JoinColumn(name = "type", nullable = false)
    var type: RefactoringType = RefactoringType(),

    @Column(name = "data", nullable = false, columnDefinition = "text")
    var data: Data = Data(type),

    @Column(name = "description", nullable = false, columnDefinition = "text")
    var description: String = "",

    @Column(name = "created", nullable = false)
    var created: Date = Date(),

    @Column(name = "last_modified", nullable = false)
    var lastModified: Date = Date(),

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    val id: Long = 0
) {
    @JsonIgnore
    @OneToMany(mappedBy = "parent")
    @Column(name = "children", nullable = false)
    val children: MutableSet<Refactoring> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.REMOVE])
    @Column(name = "draft_children", nullable = false)
    val draftChildren: MutableSet<Draft> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "origin", cascade = [CascadeType.REMOVE])
    @Column(name = "drafts", nullable = false)
    val drafts: MutableSet<Draft> = mutableSetOf()

    @PostUpdate
    private fun onPostUpdate() {
        lastModified = Date()
    }

    @PreRemove
    private fun onPreRemove() {
        children.forEach { it.parent = null }
        draftChildren.forEach { it.parent = null }
        drafts.forEach { it.origin = null }
    }

    data class Data(
        val before: MutableMap<String, Element> = mutableMapOf(),
        val after: MutableMap<String, Element> = mutableMapOf()
    ) : Serializable {
        constructor(type: RefactoringType) : this(
            type.before.entries.associateBy({ it.key }) { it.value.dataClass.createInstance() }.toMutableMap(),
            type.after.entries.associateBy({ it.key }) { it.value.dataClass.createInstance() }.toMutableMap()
        )
    }
}
