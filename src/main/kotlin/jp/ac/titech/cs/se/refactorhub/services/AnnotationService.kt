package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.models.Commit
import jp.ac.titech.cs.se.refactorhub.models.User
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

    fun create(owner: User, commit: Commit) = annotationRepository.save(Annotation(owner, commit))

    fun fork(owner: User, parent: Annotation): Annotation = annotationRepository.save(
        Annotation(
            owner,
            parent.commit,
            parent,
            parent.type,
            parent.refactoring,
            parent.description
        )
    )

    fun save(annotation: Annotation): Annotation = annotationRepository.save(annotation)

    fun delete(id: Long) = annotationRepository.deleteById(id)

}
