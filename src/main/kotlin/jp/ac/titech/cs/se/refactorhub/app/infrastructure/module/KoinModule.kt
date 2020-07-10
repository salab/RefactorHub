package jp.ac.titech.cs.se.refactorhub.app.infrastructure.module

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.locations.KtorExperimentalLocationsAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.UserRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.CommitController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.ElementController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringDraftController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.RefactoringTypeController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.dsl.module

@KtorExperimentalLocationsAPI
val koinModules = module {
    single { jacksonObjectMapper() }
    single { UserController() }
    single { CommitController() }
    single { RefactoringController() }
    single { RefactoringTypeController() }
    single { RefactoringDraftController() }
    single { ElementController() }
    single { UserService() }
    single<UserRepository> { UserRepositoryImpl() }
}
