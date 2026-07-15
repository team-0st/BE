package com.zerost.api.common.config

import com.zerost.api.common.device.DeviceIdInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val deviceIdInterceptor: DeviceIdInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(deviceIdInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns("/api/v1/users/register")
    }
}
