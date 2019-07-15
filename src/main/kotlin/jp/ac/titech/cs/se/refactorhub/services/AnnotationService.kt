package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.repositories.AnnotationRepository
import org.springframework.stereotype.Service

@Service
class AnnotationService(
    private val annotationRepository: AnnotationRepository
) {

    fun get(id: Long): Annotation {
        val annotation = annotationRepository.findById(id)
        if (annotation.isPresent) return annotation.get()
        throw NotFoundException("Annotation(id=$id) is not found.")
    }

    fun create(draft: Draft) = annotationRepository.save(
        Annotation(
            draft.owner,
            draft.commit,
            draft.parent,
            draft.type,
            draft.refactoring,
            draft.description
        )
    )

    fun save(annotation: Annotation): Annotation = annotationRepository.save(annotation)

    fun delete(id: Long) = annotationRepository.deleteById(id)

}
