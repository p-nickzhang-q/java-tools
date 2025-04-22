package zh.tools.jpa

import cn.hutool.extra.spring.SpringUtil
import java.io.Serializable

interface PersistentEntity<T, ID : Serializable> {

    val clazz: Class<out BaseFilterService<T, ID>>

    fun save() {
        SpringUtil.getBean(clazz).save(this as T)
    }

    fun remove() {
        SpringUtil.getBean(clazz).remove(this as T)
    }
}