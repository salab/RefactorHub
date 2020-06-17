package jp.ac.titech.cs.se.refactorhub.config.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.RefactoringType
import jp.ac.titech.cs.se.refactorhub.models.User
import jp.ac.titech.cs.se.refactorhub.services.CommitService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataConfig(
    private val userService: UserService,
    private val commitService: CommitService,
    private val refactoringService: RefactoringService,
    private val refactoringTypeService: RefactoringTypeService
) {

    @Profile("dev", "prod")
    @Bean
    fun initializeData() = InitializingBean {
        createRefactoringTypes("data/types.json")
        createRefactoringTypes("rminer/types.json")
        createTutorial()
        createExperiment()
    }

    private fun createRefactoringTypes(path: String) {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: return
        val types = jacksonObjectMapper().readValue<List<RefactoringType>>(stream)
        types.forEach { refactoringTypeService.create(it.name, it.before, it.after) }
    }

    private fun createTutorial() {
        createRefactorings(userService.create(1, "tutorial"), "data/tutorial.json")
    }

    private fun createExperiment() {
        createRefactorings(userService.create(2, "experiment"), "data/experiment.json")
    }

    private fun createRefactorings(user: User, path: String) {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: return
        val refactorings = jacksonObjectMapper().readValue<List<RefactoringMetadata>>(stream)
        userService.getRefactorings(user.id).run {
            if (refactorings.size != this.size) {
                this.forEach { refactoringService.delete(it.id) }
                refactorings.forEach {
                    refactoringService.save(
                        Refactoring(
                            user,
                            commitService.createByUrl(it.url),
                            type = refactoringTypeService.getByName(it.type),
                            description = it.description
                        )
                    )
                }
            }
        }
    }
}
