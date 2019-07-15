package jp.ac.titech.cs.se.refactorhub.models

import jp.ac.titech.cs.se.refactorhub.models.refactoring.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.refactoring.impl.Custom
import javax.persistence.*

@Entity
@Table(name = "draft")
data class Draft(
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

    @Id
    @Column(name = "id", nullable = false)
    val id: Long = 0
)
