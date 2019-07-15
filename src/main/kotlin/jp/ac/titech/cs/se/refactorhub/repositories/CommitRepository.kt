package jp.ac.titech.cs.se.refactorhub.repositories

import jp.ac.titech.cs.se.refactorhub.models.Commit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface CommitRepository : JpaRepository<Commit, String> {
    fun findBySha(sha: String): Optional<Commit>
    fun existsBySha(sha: String): Boolean
    @Transactional
    fun deleteBySha(sha: String)
}
