package jp.ac.titech.cs.se.refactorhub.app.interfaces.controller

import jp.ac.titech.cs.se.refactorhub.app.model.AnnotationData
import jp.ac.titech.cs.se.refactorhub.app.model.Experiment
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.AnnotationService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ExperimentService
import jp.ac.titech.cs.se.refactorhub.core.model.Commit
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

@KoinApiExtension
class ExperimentController : KoinComponent {
    private val experimentService: ExperimentService by inject()
    private val annotationService: AnnotationService by inject()

    fun get(experimentId: UUID): Experiment {
        return experimentService.get(experimentId)
    }
    fun getAll(): List<Experiment> {
        return experimentService.getAll()
    }

    fun create(
        ownerId: UUID?,
        title: String,
        description: String,
        isActive: Boolean,
        targetCommits: List<Commit>
    ): Experiment {
        return experimentService.create(ownerId, title, description, isActive, targetCommits)
    }

    fun startAnnotation(userId: UUID?, experimentId: UUID, commitId: UUID): UUID {
        experimentService.verifyIds(experimentId, commitId)
        return annotationService.createAnnotationIfNotExist(userId, experimentId, commitId).id
    }

    fun getExperimentResult(userId: UUID?, experimentId: UUID): List<AnnotationData> {
        return annotationService.getExperimentAnnotationsData(userId, experimentId)
    }
}
