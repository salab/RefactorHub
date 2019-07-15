package jp.ac.titech.cs.se.refactorhub.repositories

import jp.ac.titech.cs.se.refactorhub.models.Draft
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DraftRepository : JpaRepository<Draft, Long>