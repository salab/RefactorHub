package jp.ac.titech.cs.se.refactorhub.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "user")
data class User(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = ""
) : UserDetails {
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    @Column(name = "refactorings", nullable = false)
    val refactorings: MutableList<Refactoring> = ArrayList()

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    @Column(name = "drafts", nullable = false)
    val drafts: MutableList<Draft> = ArrayList()

    @JsonIgnore
    override fun getUsername() = name

    @JsonIgnore
    override fun getPassword() = "N/A"

    @JsonIgnore
    override fun getAuthorities() = emptySet<GrantedAuthority>()

    @JsonIgnore
    override fun isEnabled() = true

    @JsonIgnore
    override fun isAccountNonExpired() = true

    @JsonIgnore
    override fun isAccountNonLocked() = true

    @JsonIgnore
    override fun isCredentialsNonExpired() = true
}
