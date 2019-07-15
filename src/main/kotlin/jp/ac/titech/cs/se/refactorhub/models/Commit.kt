package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
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
) {
    @JsonIgnore
    @OneToMany(mappedBy = "commit")
    @Column(name = "annotations", nullable = false)
    val annotations: MutableSet<Annotation> = mutableSetOf()
}
