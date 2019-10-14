package jp.ac.titech.cs.se.refactorhub.config

import jp.ac.titech.cs.se.refactorhub.models.Annotation
import jp.ac.titech.cs.se.refactorhub.services.AnnotationService
import jp.ac.titech.cs.se.refactorhub.services.CommitService
import jp.ac.titech.cs.se.refactorhub.services.DraftService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataConfiguration(
    private val annotationService: AnnotationService,
    private val commitService: CommitService,
    private val draftService: DraftService,
    private val userService: UserService
) {

    @Profile("dev")
    @Bean
    fun initializeDevData() = InitializingBean {
        val admin = userService.create(1, "admin")
        val users = (2..4L).map { userService.create(it, "user-$it") }
        listOf(
            commitService.create("f35b2c8eb8c320f173237e44d04eefb4634649a2", "danilofes", "refactoring-toy-example"),
            commitService.create("7655200f58293e5a30bf8b3cbb29ebadae374564", "JetBrains", "intellij-community")
        ).forEach { commit ->
            val annotation = annotationService.save(
                Annotation(
                    admin,
                    commit,
                    description = "This is a sample of ExtractMethod."
                )
            )
            users.forEach { draftService.fork(it, annotation) }
        }
    }
}