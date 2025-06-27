package zh.tools.maptosql

// 4. 主转换器类
open class JsonToSqlConverter(
    private val dialect: SqlDialect,
    private val entityMappings: Map<String, EntityMapping> = emptyMap()
) {

    constructor(dialect: SqlDialect, vararg entityClasses: Class<*>) : this(
        dialect,
        AnnotationParser.parseMappings(*entityClasses)
    )

    fun convertToSelect(
        entityName: String,
        jsonCriteria: Map<String, Any?>,
        selectFields: String? = null,
        orderBy: String? = null,
        offset: Int? = null,
        limit: Int? = null
    ): String {
        val mapping = entityMapping(entityName)

        val tableName = dialect.quoteIdentifier(mapping.tableName)
        val fields = selectFields ?: mapping.defaultSelectFields

        val whereClause = buildWhereClause(jsonCriteria, mapping.fieldMappings)

        val sql = buildString {
            append("SELECT $fields FROM $tableName")
            if (whereClause.isNotEmpty()) append(" WHERE $whereClause")
            if (!orderBy.isNullOrEmpty()) append(" ORDER BY ${dialect.quoteIdentifier(orderBy)}")
            if (offset != null && limit != null) append(" ${dialect.pagination(offset, limit)}")
        }

        return sql
    }

    fun entityMapping(entityName: String) =
        entityMappings[entityName] ?: throw IllegalArgumentException("Unknown entity: $entityName")


    fun convertToWhereClause(
        entityName: String,
        jsonCriteria: Map<String, Any?>
    ): String {
        val mapping = entityMappings[entityName] ?: throw IllegalArgumentException("Unknown entity: $entityName")
        return buildWhereClause(jsonCriteria, mapping.fieldMappings)
    }

    private fun buildWhereClause(criteria: Map<String, Any?>, fieldMappings: Map<String, String>): String {
        val conditions = mutableListOf<String>()

        criteria.entries.filter { it.value != null }.forEach { (key, value) ->
            when (key) {
                // 处理逻辑操作符 OR/AND
                "\$or", "\$and" -> {
                    val subConditions = (value as List<Map<String, Any?>>)
                        .map { subCriteria ->
                            val clause = buildWhereClause(subCriteria, fieldMappings)
                            if (subCriteria.size > 1) "($clause)" else clause
                        }
                        .filter { it.isNotEmpty() }

                    if (subConditions.isNotEmpty()) {
                        conditions.add("(${subConditions.joinToString(if (key == "\$or") " OR " else " AND ")})")
                    }
                }

                // 处理普通字段条件
                else -> {
                    val columnName = fieldMappings[key] ?: key
                    when (value) {
                        // 处理嵌套操作符（如 $gt、$in 等）
                        is Map<*, *> -> {
                            (value as Map<String, Any>).entries.joinToString(" AND ") { (op, opValue) ->
                                buildOperatorCondition(columnName, op, opValue)
                            }.takeIf { it.isNotEmpty() }?.let { conditions.add(it) }
                        }

                        // 处理直接值（如 name = "John"）
                        else -> {
                            conditions.add("${dialect.quoteIdentifier(columnName)} = ${dialect.formatValue(value!!)}")
                        }
                    }
                }
            }
        }

        return when {
            conditions.isEmpty() -> ""
            conditions.size == 1 -> conditions.first()
            else -> conditions.joinToString(" AND ")
        }
    }

    private fun buildOperatorCondition(columnName: String, op: String, opValue: Any): String {
        return when (op) {
            "\$gt" -> "${dialect.quoteIdentifier(columnName)} > ${dialect.formatValue(opValue)}"
            "\$gte" -> "${dialect.quoteIdentifier(columnName)} >= ${dialect.formatValue(opValue)}"
            "\$lt" -> "${dialect.quoteIdentifier(columnName)} < ${dialect.formatValue(opValue)}"
            "\$lte" -> "${dialect.quoteIdentifier(columnName)} <= ${dialect.formatValue(opValue)}"
            "\$between" -> {
                val (start, end) = opValue as List<Any>
                "${dialect.quoteIdentifier(columnName)} BETWEEN ${dialect.formatValue(start)} AND ${
                    dialect.formatValue(
                        end
                    )
                }"
            }

            "\$like" -> "${dialect.quoteIdentifier(columnName)} LIKE ${dialect.formatValue(opValue)}"
            "\$notEqual" -> "${dialect.quoteIdentifier(columnName)} != ${dialect.formatValue(opValue)}"
            "\$notNull" -> "${dialect.quoteIdentifier(columnName)} IS NOT NULL"
            "\$isNull" -> "${dialect.quoteIdentifier(columnName)} IS NULL"
            "\$in" -> "${dialect.quoteIdentifier(columnName)} IN ${dialect.formatValue(opValue)}"
            else -> throw IllegalArgumentException("Unsupported operator: $op")
        }
    }
}