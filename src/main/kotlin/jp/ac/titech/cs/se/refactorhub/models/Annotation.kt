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

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    val id: Long = 0
) {
    @JsonIgnore
    @OneToMany(mappedBy = "parent")
    @Column(name = "children", nullable = false)
    val children: MutableSet<Annotation> = mutableSetOf()

    @PostUpdate
    private fun onPostUpdate() {
        lastModified = Date()
    }

    @PreRemove
    private fun onPreRemove() {
        children.forEach { it.parent = null }
    }
}
