package jp.ac.titech.cs.se.refactorhub.repositories

import jp.ac.titech.cs.se.refactorhub.models.Annotation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AnnotationRepository : JpaRepository<Annotation, Long> {
    @Query("SELECT a FROM Annotation a JOIN FETCH a.children WHERE a.id = (:id)")
    fun findByIdAndFetchChildrenEagerly(@Param("id") id: Long): Optional<Annotation>

    @Query("SELECT a FROM Annotation a JOIN FETCH a.drafts WHERE a.id = (:id)")
    fun findByIdAndFetchDraftsEagerly(@Param("id") id: Long): Optional<Annotation>
}
