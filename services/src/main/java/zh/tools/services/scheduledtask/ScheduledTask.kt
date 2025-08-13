package zh.tools.services.scheduledtask

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.coroutines.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
interface ScheduledTask {
    val id: String
    val executeAt: LocalDateTime
    fun execute()
}

// 提供配置类
object TaskModule : SimpleModule() {
    private val subtypes = mutableMapOf<String, Class<out ScheduledTask>>()

    fun registerSubtype(typeName: String, taskClass: Class<out ScheduledTask>) {
        subtypes[typeName] = taskClass
    }

    override fun setupModule(context: SetupContext) {
        context.registerSubtypes(
            *subtypes.map { (name, clazz) ->
                NamedType(clazz, name)
            }.toTypedArray()
        )
    }
}

@Service
class TaskSchedulerService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val scheduledTasksKey = "scheduled:tasks"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // 启动任务检查协程
        scope.launch {
            while (isActive) {
                checkAndExecuteTasks()
                delay(1000) // 每秒检查一次
            }
        }
    }

    // 添加定时任务
    fun scheduleTask(task: ScheduledTask): Boolean {
        val executeAtEpochSecond = task.executeAt.atZone(ZoneId.systemDefault()).toEpochSecond()
        val serializedTask = objectMapper.writeValueAsString(task)

        return redisTemplate.opsForZSet()
            .add(scheduledTasksKey, serializedTask, executeAtEpochSecond.toDouble())!!
    }

    // 取消定时任务
    fun cancelTask(taskId: String): Boolean {
        // 需要先找到对应的任务再删除
        val tasks = redisTemplate.opsForZSet()
            .range(scheduledTasksKey, 0, -1)?.toList() ?: emptyList()

        for (serializedTask in tasks) {
            val task = objectMapper.readValue(serializedTask, ScheduledTask::class.java)
            if (task.id == taskId) {
                return redisTemplate.opsForZSet()
                    .remove(scheduledTasksKey, serializedTask)!! > 0
            }
        }
        return false
    }

    // 检查并执行到期任务
    private suspend fun checkAndExecuteTasks() {
        val currentEpochSecond = Instant.now().epochSecond
        val serializedTasks = redisTemplate.opsForZSet()
            .rangeByScore(scheduledTasksKey, 0.0, currentEpochSecond.toDouble())

        serializedTasks?.forEach { serializedTask ->
            try {
                val task = objectMapper.readValue(serializedTask, ScheduledTask::class.java)

                // 使用原子操作移除任务
                val removed = redisTemplate.opsForZSet()
                    .remove(scheduledTasksKey, serializedTask)

                if (removed!! > 0) {
                    // 在IO线程池中执行任务
                    withContext(Dispatchers.IO) {
                        try {
                            task.execute()
                        } catch (e: Exception) {
                            println("任务执行失败: ${task.id}, 错误: ${e.message}")
                            // 可以添加重试逻辑或错误处理
                        }
                    }
                }
            } catch (e: Exception) {
                println("任务反序列化失败: $serializedTask, 错误: ${e.message}")
                // 移除无效任务
                redisTemplate.opsForZSet().remove(scheduledTasksKey, serializedTask)
            }
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}