package zh.tools.maptosql

// 1. 定义数据库方言接口
interface SqlDialect {
    fun quoteIdentifier(identifier: String): String
    fun formatValue(value: Any): String
    fun pagination(offset: Int, limit: Int): String
    fun formatList(list: List<*>): String {
        return list.joinToString(
            prefix = "(",
            postfix = ")",
            transform = { item ->
                formatValue(item!!)
            }
        )
    }
}

// 2. 实现常见数据库方言
class MySqlDialect : SqlDialect {
    override fun quoteIdentifier(identifier: String) = "`$identifier`"
    override fun formatValue(value: Any) = when (value) {
        is String -> "'${value.replace("'", "''")}'"
        is Number -> value.toString()
        is Boolean -> if (value) "1" else "0"
        is List<*> -> formatList(value)
        else -> "'$value'"
    }

    override fun pagination(offset: Int, limit: Int) = "LIMIT $limit OFFSET $offset"
}

class PostgreSqlDialect : SqlDialect {
    override fun quoteIdentifier(identifier: String) = "\"$identifier\""
    override fun formatValue(value: Any) = when (value) {
        is String -> "'${value.replace("'", "''")}'"
        is Number -> value.toString()
        is Boolean -> if (value) "TRUE" else "FALSE"
        is List<*> -> formatList(value)
        else -> "'$value'"
    }

    override fun pagination(offset: Int, limit: Int) = "LIMIT $limit OFFSET $offset"
}

class OracleDialect : SqlDialect {
    override fun quoteIdentifier(identifier: String) = "\"$identifier\""
    override fun formatValue(value: Any) = when (value) {
        is String -> "'${value.replace("'", "''")}'"
        is Number -> value.toString()
        is Boolean -> if (value) "1" else "0"
        is List<*> -> formatList(value)
        else -> "'$value'"
    }

    override fun pagination(offset: Int, limit: Int) = "OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY"
}