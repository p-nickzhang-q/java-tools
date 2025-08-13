package zh.tools.services.scheduledtask

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@ConditionalOnClass(RedisTemplate::class)
open class TaskSchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun taskSchedulerService(
        redisTemplate: RedisTemplate<String, String>,
        objectMapper: ObjectMapper
    ): TaskSchedulerService {
        return TaskSchedulerService(redisTemplate, objectMapper)
    }
}