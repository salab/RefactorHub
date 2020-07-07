package jp.ac.titech.cs.se.refactorhub.old.repositories

import jp.ac.titech.cs.se.refactorhub.old.models.RefactoringType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefactoringTypeRepository : JpaRepository<RefactoringType, Long> {
    fun findByName(name: String): Optional<RefactoringType>
}
