package zh.tools.mybatisplus

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import zh.tools.maptosql.JsonToSqlConverter
import zh.tools.maptosql.QueryBuilder
import zh.tools.maptosql.json

class SqlQueryHelper(
    private val jsonToSqlConverter: JsonToSqlConverter,
    private val className: String,
) {
    fun <M : BaseMapper<T>, T>
            ServiceImpl<M, T>.listBySql(block: QueryBuilder.() -> Unit): List<T> {
        val sql = jsonToSqlConverter.convertToWhereClause(className, json(block))
        return query().apply(sql).list().toList()
    }

    fun <M : BaseMapper<T>, T>
            ServiceImpl<M, T>.getOneBySql(block: QueryBuilder.() -> Unit): T? {
        val sql = jsonToSqlConverter.convertToWhereClause(className, json(block))
        return query().apply(sql).list().firstOrNull()
    }

    fun <M : BaseMapper<T>, T>
            ServiceImpl<M, T>.listBySql(json: Map<String, Any>, callBack: (QueryWrapper<T>) -> Unit = {}): List<T> {
        val sql = jsonToSqlConverter.convertToWhereClause(className, json)
        var query = QueryWrapper<T>()
        if (sql.isNotEmpty()) {
            query = query.apply(sql)
        }
        callBack(query)
        return list(query)
    }

    fun <M : BaseMapper<T>, T>
            ServiceImpl<M, T>.pageBySql(
        json: Map<String, Any>,
        page: Long,
        pageSize: Long,
        callBack: (QueryWrapper<T>) -> Unit = {},
    ): Page<T> {
        val sql = jsonToSqlConverter.convertToWhereClause(className, json)
        val pageObj = Page<T>(page, pageSize)
        val queryWrapper = if (sql.isNotEmpty()) {
            QueryWrapper<T>().apply(sql)
        } else {
            QueryWrapper()
        }
        callBack(queryWrapper)
        return page(pageObj, queryWrapper)
    }
}