package jp.ac.titech.cs.se.refactorhub.app.infrastructure.module

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.locations.KtorExperimentalLocationsAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ActionController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.AnnotatorController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CodeElementController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CommitController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ExperimentController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringDraftController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringTypeController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.*
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ActionService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.AnnotatorService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CodeElementService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.CommitService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.ExperimentService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringDraftService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@KoinApiExtension
@KtorExperimentalLocationsAPI
object KoinModule {
    val modules by lazy {
        listOf(
            module {
                single { jacksonObjectMapper() }
                single { UserController() }
                single { CommitController() }
                single { RefactoringController() }
                single { RefactoringTypeController() }
                single { RefactoringDraftController() }
                single { CodeElementController() }
                single { AnnotatorController() }
                single { ExperimentController() }
                single { ActionController() }
                single { UserService() }
                single { CommitService() }
                single { RefactoringService() }
                single { RefactoringTypeService() }
                single { RefactoringDraftService() }
                single { CodeElementService() }
                single { AnnotatorService() }
                single { ExperimentService() }
                single { ActionService() }
                single<UserRepository> { UserRepositoryImpl() }
                single<CommitRepository> { CommitRepositoryImpl() }
                single<ExperimentRepository> { ExperimentRepositoryImpl() }
                single<ChangeTypeRepository> { ChangeTypeRepositoryImpl() }
                single<ChangeRepository> { ChangeRepositoryImpl() }
                single<SnapshotRepository> { SnapshotRepositoryImpl() }
                single<AnnotationRepository> { AnnotationRepositoryImpl() }
                single<ActionRepository> { ActionRepositoryImpl() }
            }
        )
    }
}
