package jp.ac.titech.cs.se.refactorhub.config

import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.RefactoringType
import jp.ac.titech.cs.se.refactorhub.services.CommitService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataConfiguration(
    private val refactoringService: RefactoringService,
    private val refactoringTypeService: RefactoringTypeService,
    private val commitService: CommitService,
    private val userService: UserService
) {

    @Profile("dev")
    @Bean
    fun initializeDevData() = InitializingBean {
        val admin = userService.create(1, "admin")
        val type = refactoringTypeService.save(
            RefactoringType(
                "ExtractMethod",
                mutableMapOf(),
                mutableMapOf()
            )
        )
        listOf(
            commitService.create("f35b2c8eb8c320f173237e44d04eefb4634649a2", "danilofes", "refactoring-toy-example"),
            commitService.create("7655200f58293e5a30bf8b3cbb29ebadae374564", "JetBrains", "intellij-community")
        ).forEach { commit ->
            refactoringService.save(
                Refactoring(
                    admin,
                    commit,
                    type = type,
                    description = "This is a sample of ExtractMethod."
                )
            )
        }
    }

}