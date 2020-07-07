package jp.ac.titech.cs.se.refactorhub.old.repositories

import jp.ac.titech.cs.se.refactorhub.old.models.Draft
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DraftRepository : JpaRepository<Draft, Long>
