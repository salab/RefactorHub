package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "commit")
data class Commit(
    @Id
    @Column(name = "sha", nullable = false, length = 40)
    val sha: String = "",

    @Column(name = "owner", nullable = false, length = 100)
    var owner: String = "",

    @Column(name = "repository", nullable = false, length = 100)
    var repository: String = ""
) {
    @JsonIgnore
    @OneToMany(mappedBy = "commit")
    @Column(name = "annotations", nullable = false)
    val refactorings: MutableList<Refactoring> = ArrayList()

    @JsonIgnore
    @OneToMany(mappedBy = "commit")
    @Column(name = "drafts", nullable = false)
    val drafts: MutableList<Draft> = ArrayList()

    data class Info(
        val sha: String,
        val owner: String,
        val repository: String,
        val url: String,
        val message: String,
        val author: String,
        val authorDate: Date,
        val files: List<File>,
        val parent: String
    ) {
        data class File(
            val sha: String,
            val status: String,
            val name: String,
            val previousName: String
        )
    }
}
