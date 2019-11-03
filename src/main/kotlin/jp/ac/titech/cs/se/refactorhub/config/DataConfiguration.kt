package jp.ac.titech.cs.se.refactorhub.config

import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.RefactoringType
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.models.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.services.CommitService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringService
import jp.ac.titech.cs.se.refactorhub.services.RefactoringTypeService
import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*

@Configuration
class DataConfiguration(
    private val refactoringService: RefactoringService,
    private val refactoringTypeService: RefactoringTypeService,
    private val commitService: CommitService,
    private val userService: UserService
) {

    @Profile("dev", "prod")
    @Bean
    fun initializeDevData() = InitializingBean {
        val admin = userService.create(1, "admin")
        val type = refactoringTypeService.save(
            RefactoringType(
                "ExtractMethod",
                TreeMap<String, Element.Type>().apply {
                    put("sourceClass", Element.Type.ClassDeclaration)
                    put("source", Element.Type.Statements)
                },
                TreeMap<String, Element.Type>().apply {
                    put("targetClass", Element.Type.ClassDeclaration)
                    put("extractedMethod", Element.Type.MethodDeclaration)
                    put("invocation", Element.Type.MethodInvocation)
                }
            )
        )
        refactoringService.save(
            Refactoring(
                admin,
                commitService.create("f35b2c8eb8c320f173237e44d04eefb4634649a2", "danilofes", "refactoring-toy-example"),
                type = type,
                data = Refactoring.Data(type).apply {
                    before["sourceClass"] = ClassDeclaration()

                },
                description = "This is a sample of ExtractMethod."
            )
        )
        refactoringService.save(
            Refactoring(
                admin,
                commitService.create("7655200f58293e5a30bf8b3cbb29ebadae374564", "JetBrains", "intellij-community"),
                type = refactoringTypeService.save(RefactoringType("Custom")),
                description = "This is a sample of Custom."
            )
        )
    }

}