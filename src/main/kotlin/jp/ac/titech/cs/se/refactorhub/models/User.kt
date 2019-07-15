package jp.ac.titech.cs.se.refactorhub.models

import org.springframework.security.core.AuthenticatedPrincipal
import javax.persistence.*

@Entity
@Table(name = "user")
data class User(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "name", nullable = false, unique = true)
    var name: String = ""
) {
    data class Principal(
        val attributes: Map<String, Any>
    ) : AuthenticatedPrincipal {
        val id: Long get() = attributes["id"] as Long
        override fun getName(): String = attributes["login"] as String
    }
}
