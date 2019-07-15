package jp.ac.titech.cs.se.refactorhub.config

import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

@Configuration
class WebMvcConfiguration(
    private val resourceProperties: ResourceProperties
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/**")
            .addResourceLocations(*resourceProperties.staticLocations)
            .resourceChain(false)
            .addResolver(SpaResourceResolver())
    }

    private class SpaResourceResolver : PathResourceResolver() {
        override fun getResource(resourcePath: String, location: Resource): Resource? =
            super.getResource(resourcePath, location) ?: super.getResource("index.html", location)
    }

}
