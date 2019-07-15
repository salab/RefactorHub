package jp.ac.titech.cs.se.refactorhub.repositories

import jp.ac.titech.cs.se.refactorhub.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByName(name: String): Optional<User>
}
