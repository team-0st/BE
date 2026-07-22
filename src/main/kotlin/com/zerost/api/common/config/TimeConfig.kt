package com.zerost.api.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class TimeConfig {

    @Bean
    fun appClock(): Clock = Clock.system(ZoneId.of(ASIA_SEOUL_ZONE_ID))

    companion object {
        private const val ASIA_SEOUL_ZONE_ID = "Asia/Seoul"
    }
}
