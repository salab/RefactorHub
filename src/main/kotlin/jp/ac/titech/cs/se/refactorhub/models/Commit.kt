package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.kohsuke.github.GHCommit
import java.util.*
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

    @JsonIgnore
    @OneToMany(mappedBy = "commit")
    @Column(name = "drafts", nullable = false)
    val drafts: MutableSet<Draft> = mutableSetOf()

    data class Info(
        val sha: String,
        val owner: String,
        val repository: String,
        val url: String,
        val message: String,
        val author: String,
        val authorDate: Date,
        val files: List<File>
    ) {
        data class File(
            val sha: String,
            val status: String,
            val name: String,
            val previousName: String
        )
    }
}
