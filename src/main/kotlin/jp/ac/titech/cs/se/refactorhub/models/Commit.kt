package jp.ac.titech.cs.se.refactorhub.models

import javax.persistence.*

@Entity
@Table(name = "commit")
data class Commit(
    @Id
    @Column(name = "sha", nullable = false)
    val sha: String = "",

    @Column(name = "owner", nullable = false)
    var owner: String = "",

    @Column(name = "repository", nullable = false)
    var repository: String = ""
)
