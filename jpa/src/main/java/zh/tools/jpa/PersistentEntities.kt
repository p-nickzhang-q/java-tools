package zh.tools.jpa

import cn.hutool.extra.spring.SpringUtil
import org.springframework.data.domain.Page
import java.io.Serializable
import java.util.*

interface PersistentEntities<T, ID : Serializable> {

    val clazz: Class<out BaseFilterService<T, ID>>
    var list: List<T>

    @JvmDefault
    fun save() {
        service().save(list)
    }

    @JvmDefault
    fun remove() {
        service().remove(list)
    }

    open fun get(): List<T> {
        return list
    }

    fun service(): BaseFilterService<T, ID> = SpringUtil.getBean(clazz)

    fun filter(jpaFilterRequest: JpaFilterRequest): Page<T> {
        val filter = service().filter(jpaFilterRequest)
        list = filter.content
        return filter
    }

    fun filter(filter: Map<String, Any>) {
        list = service().filter(filter)
    }

    fun all() {
        list = service().filter()
    }

    fun filterOne(filter: Map<String, Any>): Optional<T> {
        return service().filterOne(filter)
    }

    fun count(filter: Map<String, Any>): Long {
        return service().count(filter)
    }

    fun removeById(id: ID) {
        service().removeById(id)
    }

    fun removeByIds(ids: List<ID>) {
        service().removeByIds(ids)
    }

    fun findById(id: ID): Optional<T> {
        return service().findById(id);
    }

    fun findByIds(ids: List<ID>) {
        list = service().repository().findAllById(ids).toList();
    }
}