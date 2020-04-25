package jp.ac.titech.cs.se.refactorhub.config

import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.element.Element
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
    fun initializeData() = InitializingBean {
        // users
        val tutorial = userService.create(1, "tutorial")
        val experiment = userService.create(2, "experiment")

        // types
        val extractMethod = refactoringTypeService.create(
            "ExtractMethod",
            TreeMap<String, Element.Type>().apply {
                put("sourceClass", Element.Type.ClassDeclaration)
                put("source", Element.Type.CodeFragments)
            },
            TreeMap<String, Element.Type>().apply {
                put("targetClass", Element.Type.ClassDeclaration)
                put("extractedMethod", Element.Type.MethodDeclaration)
                put("invocation", Element.Type.MethodInvocation)
            }
        )
        val custom = refactoringTypeService.create("Custom")

        // refactorings
        val tutorials = listOf(
            Refactoring(
                tutorial,
                commitService.createByUrl("https://github.com/danilofes/refactoring-toy-example/commit/f35b2c8eb8c320f173237e44d04eefb4634649a2"),
                type = extractMethod,
                description = "This is a tutorial of ExtractMethod annotation."
            ),
            // http://refactoring.encs.concordia.ca/oracle/commit-refactorings/1100868
            Refactoring(
                tutorial,
                commitService.create("7655200f58293e5a30bf8b3cbb29ebadae374564", "JetBrains", "intellij-community"),
                type = custom,
                description = "This is a tutorial of Custom annotation."
            )
        )
        userService.getRefactorings(tutorial.id).run {
            if (tutorials.size != this.size) {
                this.forEach { refactoringService.delete(it.id) }
                tutorials.forEach { refactoringService.save(it) }
            }
        }

        val experiments = listOf(
            Refactoring(
                experiment,
                commitService.createByUrl("https://github.com/Athou/commafeed/commit/18a7bd1fd1a83b3b8d1b245e32f78c0b4443b7a7"),
                type = extractMethod,
                description = "Extract Method private fetch(url String) : byte[] extracted from public fetch(feed Feed) : byte[] in class com.commafeed.backend.favicon.DefaultFaviconFetcher"
            ),
            Refactoring(
                experiment,
                commitService.createByUrl("https://github.com/fabric8io/fabric8/commit/9e61a71540da58c3208fd2c7737f793c3f81e5ae"),
                type = extractMethod,
                description = "Extract Method public createGogsWebhook(kubernetes KubernetesClient, log Log, gogsUser String, gogsPwd String, repoName String, webhookUrl String, webhookSecret String) : boolean extracted from public execute() : void in class io.fabric8.maven.CreateGogsWebhook"
            ),
            Refactoring(
                experiment,
                commitService.createByUrl("https://github.com/Netflix/eureka/commit/f6212a7e474f812f31ddbce6d4f7a7a0d498b751"),
                type = extractMethod,
                description = "Extract Method protected onRemoteStatusChanged(oldStatus InstanceInfo.InstanceStatus, newStatus InstanceInfo.InstanceStatus) : void extracted from private updateInstanceRemoteStatus() : void in class com.netflix.discovery.DiscoveryClient"
            ),
            Refactoring(
                experiment,
                commitService.createByUrl("https://github.com/infinispan/infinispan/commit/e3b0d87b3ca0fd27cec39937cb3dc3a05b0cfc4e"),
                type = extractMethod,
                description = "Extract Method protected waitForCacheToStabilize(cache Cache<Object,Object>, cacheConfig Configuration) : void extracted from public perform(ctx InvocationContext) : Object in class org.infinispan.commands.CreateCacheCommand"
            ),
            Refactoring(
                experiment,
                commitService.createByUrl("https://github.com/brianfrankcooper/YCSB/commit/0b024834549c53512ef18bce89f60ef9225d4819"),
                type = extractMethod,
                description = "Extract Method private throttle(currTimeMillis long) : void extracted from public run() : void in class com.yahoo.ycsb.ClientThread"
            )
        )
        userService.getRefactorings(experiment.id).run {
            if (experiments.size != this.size) {
                this.forEach { refactoringService.delete(it.id) }
                experiments.forEach { refactoringService.save(it) }
            }
        }
    }

}