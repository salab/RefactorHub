package jp.ac.titech.cs.se.refactorhub.app.infrastructure.module

import io.ktor.locations.KtorExperimentalLocationsAPI
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.repository.UserRepositoryImpl
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import jp.ac.titech.cs.se.refactorhub.app.interfaces.repository.UserRepository
import jp.ac.titech.cs.se.refactorhub.app.usecase.service.UserService
import org.koin.dsl.module

@KtorExperimentalLocationsAPI
val koinModules = module {
    single { UserController() }
    single { UserService() }
    single<UserRepository> { UserRepositoryImpl() }
}
