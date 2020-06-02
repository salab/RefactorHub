package jp.ac.titech.cs.se.refactorhub.repositories

import jp.ac.titech.cs.se.refactorhub.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByName(name: String): Optional<User>

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.drafts WHERE u.id = (:id)")
    fun findByIdAndFetchDraftsEagerly(@Param("id") id: Long): Optional<User>

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refactorings WHERE u.id = (:id)")
    fun findByIdAndFetchRefactoringsEagerly(@Param("id") id: Long): Optional<User>
}
