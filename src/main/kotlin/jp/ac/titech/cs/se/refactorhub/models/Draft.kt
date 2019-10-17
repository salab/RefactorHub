package jp.ac.titech.cs.se.refactorhub.models

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
    var parent: Refactoring? = null,

    @ManyToOne
    @JoinColumn(name = "origin", nullable = true)
    var origin: Refactoring? = null,

    @ManyToOne
    @JoinColumn(name = "type", nullable = false)
    var type: RefactoringType = RefactoringType(),

    @Column(name = "data", nullable = false, columnDefinition = "text")
    var data: Refactoring.Data = Refactoring.Data(type),

    @Column(name = "description", nullable = false, columnDefinition = "text")
    var description: String = "",

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    val id: Long = 0
)
