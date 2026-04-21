package io.github.sanyavertolet.edukate.gateway.configs

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val manager = CaffeineCacheManager()
        manager.setAsyncCacheMode(true)
        manager.registerCustomCache(
            "user-credentials-by-id",
            Caffeine.from("maximumSize=500,expireAfterWrite=5m").recordStats().buildAsync(),
        )
        return manager
    }
}
