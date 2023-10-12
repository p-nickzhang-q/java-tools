package zh.tools.jpa

import cn.hutool.extra.spring.SpringUtil
import java.io.Serializable

interface PersistentEntity<T, ID : Serializable> {

    val clazz: Class<out BaseFilterService<T, ID>>

    @JvmDefault
    fun save() {
        SpringUtil.getBean(clazz).save(this as T)
    }

    @JvmDefault
    fun remove() {
        SpringUtil.getBean(clazz).remove(this as T)
    }
}