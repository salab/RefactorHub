package jp.ac.titech.cs.se.refactorhub.config

import io.swagger.models.auth.In
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.Collections.singletonList

@Configuration
@EnableSwagger2
class SwaggerConfiguration {

    @Bean
    fun petApi(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .paths(PathSelectors.ant("/api/**"))
        .build()
        .useDefaultResponseMessages(false)
        .apiInfo(apiInfo())
        .securitySchemes(singletonList(apiKey()))
        .securityContexts(singletonList(securityContext()))

    private fun apiInfo(): ApiInfo = ApiInfoBuilder()
        .title("RefactorHub")
        .build()

    private fun apiKey() = ApiKey(
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.AUTHORIZATION,
        In.HEADER.name
    )

    private fun securityContext(): SecurityContext = SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/api/.*"))
        .build()

    private fun defaultAuth(): List<SecurityReference> =
        singletonList(SecurityReference(HttpHeaders.AUTHORIZATION, arrayOf(AuthorizationScope("global", "accessEverything"))))

}
