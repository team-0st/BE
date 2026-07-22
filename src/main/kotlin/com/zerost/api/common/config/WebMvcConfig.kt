package com.zerost.api.common.config

import com.zerost.api.common.auth.AdminAuthorizationInterceptor
import com.zerost.api.common.auth.AuthenticationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val adminAuthorizationInterceptor: AdminAuthorizationInterceptor,
    private val corsProperties: CorsProperties,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns("/api/v1/users/register", "/api/v1/auth/**")

        registry.addInterceptor(adminAuthorizationInterceptor)
            .addPathPatterns("/api/v1/admin/**")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (corsProperties.allowedOrigins.isEmpty()) {
            return
        }

        registry.addMapping("/api/v1/**")
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "Authorization")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
