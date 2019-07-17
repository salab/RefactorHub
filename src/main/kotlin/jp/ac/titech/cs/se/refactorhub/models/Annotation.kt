package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jp.ac.titech.cs.se.refactorhub.models.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.refactoring.impl.Custom
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "annotation")
data class Annotation(
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    val owner: User,

    @ManyToOne
    @JoinColumn(name = "commit", nullable = false)
    val commit: Commit,

    @ManyToOne
    @JoinColumn(name = "parent", nullable = true)
    var parent: Annotation? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: Refactoring.Type = Refactoring.Type.Custom,

    @Convert(converter = Refactoring.Converter::class)
    @Column(name = "refactoring", nullable = false, columnDefinition = "text")
    var refactoring: Refactoring = Custom(),

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
    val children: MutableSet<Annotation> = mutableSetOf()

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
}
