package jp.ac.titech.cs.se.refactorhub.old.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import java.io.Serializable
import java.util.ArrayList
import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.PostUpdate
import javax.persistence.PreRemove
import javax.persistence.Table

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

    @Lob
    @Column(name = "data", nullable = false)
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
    val children: MutableList<Refactoring> = ArrayList()

    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.REMOVE])
    @Column(name = "draft_children", nullable = false)
    val draftChildren: MutableList<Draft> = ArrayList()

    @JsonIgnore
    @OneToMany(mappedBy = "origin", cascade = [CascadeType.REMOVE])
    @Column(name = "drafts", nullable = false)
    val drafts: MutableList<Draft> = ArrayList()

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
        val before: MutableMap<String, Element.Data> = mutableMapOf(),
        val after: MutableMap<String, Element.Data> = mutableMapOf()
    ) : Serializable {
        constructor(type: RefactoringType) : this(
            type.before.entries.associate {
                it.key to Element.Data(
                    it.value.type,
                    it.value.multiple,
                    it.value.required
                )
            }.toMutableMap(),
            type.after.entries.associate { it.key to Element.Data(it.value.type, it.value.multiple, it.value.required) }
                .toMutableMap()
        )
    }
}
