package com.tab.utrabajo.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${app.upload-directory:./data/uploads}") uploadDirectory: String,
) : WebMvcConfigurer {
    private val uploads = Path.of(uploadDirectory).toAbsolutePath().normalize()

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploads.toUri().toString())
    }
}
