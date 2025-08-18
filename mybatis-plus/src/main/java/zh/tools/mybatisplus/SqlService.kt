package zh.tools.mybatisplus

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import zh.tools.maptosql.JsonToSqlConverter
import zh.tools.maptosql.QueryBuilder

interface ISqlService<M : BaseMapper<T>, T> {
    val jsonToSqlConverter: JsonToSqlConverter
    val serviceImpl: ServiceImpl<M, T>
    val className: String
    fun listBySql(block: QueryBuilder.() -> Unit): List<T>

    fun getOneBySql(block: QueryBuilder.() -> Unit): T?

    fun listBySql(
        json: Map<String, Any>,
        orders: List<Pair<String, OrderType>> = emptyList(),
        callBack: (QueryWrapper<T>) -> Unit = {},
    ): List<T>

    fun <T> QueryWrapper<T>.addOrders(
        orders: List<Pair<String, OrderType>>,
    ): QueryWrapper<T>

    fun pageBySql(
        json: Map<String, Any>,
        page: Long,
        pageSize: Long,
        orders: List<Pair<String, OrderType>> = emptyList(),
        callBack: (QueryWrapper<T>) -> Unit = {},
    ): Page<T>
}

open class SqlService<M : BaseMapper<T>, T>(
    override val jsonToSqlConverter: JsonToSqlConverter,
    override val serviceImpl: ServiceImpl<M, T>,
    override val className: String,
    val sqlQueryHelper: SqlQueryHelper = SqlQueryHelper(jsonToSqlConverter, className),
) : ISqlService<M, T>, IService<T> by serviceImpl {
    override fun listBySql(block: QueryBuilder.() -> Unit): List<T> {
        return sqlQueryHelper.run {
            serviceImpl.listBySql(block)
        }
    }

    override fun getOneBySql(block: QueryBuilder.() -> Unit): T? {
        return sqlQueryHelper.run {
            serviceImpl.getOneBySql(block)
        }
    }

    override fun listBySql(
        json: Map<String, Any>,
        orders: List<Pair<String, OrderType>>,
        callBack: (QueryWrapper<T>) -> Unit,
    ): List<T> {
        return sqlQueryHelper.run {
            serviceImpl.listBySql(json, orders, callBack)
        }
    }

    override fun <T> QueryWrapper<T>.addOrders(orders: List<Pair<String, OrderType>>): QueryWrapper<T> {
        val queryWrapper = this
        return sqlQueryHelper.run {
            queryWrapper.addOrders(orders)
        }
    }

    inline fun <reified T> QueryWrapper<T>.applyWhere(noinline block: QueryBuilder.() -> Unit): QueryWrapper<T> {
        val queryWrapper = this
        return sqlQueryHelper.run {
            queryWrapper.applyWhere(block)
        }
    }

    override fun pageBySql(
        json: Map<String, Any>,
        page: Long,
        pageSize: Long,
        orders: List<Pair<String, OrderType>>,
        callBack: (QueryWrapper<T>) -> Unit,
    ): Page<T> {
        sqlQueryHelper.run {
            return serviceImpl.pageBySql(json, page, pageSize, orders, callBack)
        }
    }
}