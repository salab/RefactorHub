package jp.ac.titech.cs.se.refactorhub.app.infrastructure.module

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.locations.KtorExperimentalLocationsAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.repository.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.*
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.*
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.*
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
                single { AnnotationController() }
                single { ChangeTypeController() }
                single { CodeElementController() }
                single { ExperimentController() }
                single { ActionController() }

                single { UserService() }
                single { CommitService() }
                single { AnnotationService() }
                single { SnapshotService() }
                single { ChangeService() }
                single { ChangeTypeService() }
                single { CodeElementService() }
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
