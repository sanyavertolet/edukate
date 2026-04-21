package io.github.sanyavertolet.edukate.backend.configs

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
        listOf(
                "problems-by-id" to "maximumSize=500,expireAfterWrite=24h",
                "problems-by-key" to "maximumSize=500,expireAfterWrite=24h",
                "users-by-id" to "maximumSize=500,expireAfterWrite=10m",
                "users-by-name" to "maximumSize=500,expireAfterWrite=10m",
                "problemSets" to "maximumSize=200,expireAfterWrite=5m",
                "presigned-urls" to "maximumSize=1000,expireAfterWrite=30m",
                "books" to "maximumSize=50,expireAfterWrite=24h",
            )
            .forEach { (name, spec) -> manager.registerCustomCache(name, Caffeine.from(spec).recordStats().buildAsync()) }
        return manager
    }
}
