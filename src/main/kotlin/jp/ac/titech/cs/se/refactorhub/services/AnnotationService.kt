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

    fun getAll(): List<Annotation> = annotationRepository.findAll()

    fun get(id: Long): Annotation {
        val annotation = annotationRepository.findById(id)
        if (annotation.isPresent) return annotation.get()
        throw NotFoundException("Annotation(id=$id) is not found.")
    }

    fun getChildren(id: Long): List<Annotation> {
        val annotation = annotationRepository.findByIdAndFetchChildrenEagerly(id)
        if (annotation.isPresent) return annotation.get().children.toList()
        throw NotFoundException("Annotation(id=$id) is not found.")
    }

    fun getDrafts(id: Long): List<Draft> {
        val annotation = annotationRepository.findByIdAndFetchDraftsEagerly(id)
        if (annotation.isPresent) return annotation.get().drafts.toList()
        throw NotFoundException("Annotation(id=$id) is not found.")
    }

    fun save(draft: Draft): Annotation {
        return annotationRepository.save(
            Annotation(
                draft.owner,
                draft.commit,
                draft.parent,
                draft.type,
                draft.refactoring,
                draft.description
            )
        )
    }

    fun save(annotation: Annotation): Annotation = annotationRepository.save(annotation)

    fun delete(id: Long) = annotationRepository.deleteById(id)

}
