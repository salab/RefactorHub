package jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.HttpStatusCodeContent
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resolveResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.acceptItems
import io.ktor.request.uri
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.util.AttributeKey
import java.io.FileNotFoundException
import java.nio.file.Paths

class SinglePageApplication(private val configuration: Configuration) {

    companion object Feature : ApplicationFeature<Application, Configuration, SinglePageApplication> {

        override val key = AttributeKey<SinglePageApplication>("SinglePageApplication")

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): SinglePageApplication {

            val feature = SinglePageApplication(
                Configuration()
                    .apply(configure)
            )

            pipeline.routing {
                static(feature.configuration.spaRoute) {
                    resources(feature.configuration.staticPath)
                    defaultResource(feature.configuration.indexPagePath)
                }
            }

            pipeline.intercept(ApplicationCallPipeline.Fallback) {
                if (call.response.status() == null) {
                    call.respond(HttpStatusCodeContent(HttpStatusCode.NotFound))
                    finish()
                }
            }

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
                val uri = call.request.uri
                val shouldIgnored by lazy {
                    uri.startsWith(feature.configuration.ignorePath) ||
                        uri.startsWith("/${feature.configuration.ignorePath}")
                }
                val isSpaRoute by lazy {
                    uri.startsWith(feature.configuration.spaRoute) ||
                        uri.startsWith("/${feature.configuration.spaRoute}")
                }
                val isNotFound by lazy {
                    it is HttpStatusCodeContent && it.status == HttpStatusCode.NotFound
                }
                val acceptsHtml by lazy {
                    call.request.acceptItems().any {
                        ContentType.Text.Html.match(it.value)
                    }
                }

                if (shouldIgnored || !isSpaRoute || !isNotFound || !acceptsHtml) return@intercept

                call.attributes.put(key, feature)

                val indexPage = call.resolveResource(feature.configuration.indexPagePath)
                    ?: throw FileNotFoundException("${feature.configuration.indexPagePath} is not found")
                call.respond(indexPage)

                finish()
            }

            return feature
        }
    }

    data class Configuration(
        var spaRoute: String = "",
        var staticPath: String = "static",
        var indexPage: String = "index.html",
        var ignorePath: String = "api"
    ) {
        val indexPagePath: String get() = Paths.get(staticPath, indexPage).toString()
    }
}
