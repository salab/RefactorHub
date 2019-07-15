package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.AuthenticatedPrincipal
import javax.persistence.*

@Entity
@Table(name = "user")
data class User(
    @Id
    @Column(name = "id", nullable = false)
    val id: Int = 0,

    @Column(name = "name", nullable = false, unique = true)
    var name: String = ""
) {
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    @Column(name = "annotations", nullable = false)
    val annotations: MutableSet<Annotation> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    @Column(name = "drafts", nullable = false)
    val drafts: MutableSet<Draft> = mutableSetOf()

    data class Principal(
        val attributes: Map<String, Any>
    ) : AuthenticatedPrincipal {
        val id: Int get() = attributes["id"] as Int
        override fun getName(): String = attributes["login"] as String
    }
}
