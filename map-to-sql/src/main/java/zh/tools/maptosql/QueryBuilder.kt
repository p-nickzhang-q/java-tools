package zh.tools.maptosql

// 定义操作符类
class QueryBuilder {
    private val map = mutableMapOf<String, Any>()
    private val orConditions = mutableListOf<Map<String, Any>>()
    private val andConditions = mutableListOf<Map<String, Any>>()
    private var isInOrBlock = false
    private var isInAndBlock = false

    fun field(name: String, value: Any) {
        if (isInOrBlock) {
            orConditions.add(mapOf(name to value))
        } else if (isInAndBlock) {
            andConditions.add(mapOf(name to value))
        } else {
            map[name] = value
        }
    }

    fun field(name: String, operator: OperatorBuilder.() -> Unit) {
        val condition = OperatorBuilder().apply(operator).build()
        if (isInOrBlock) {
            orConditions.add(mapOf(name to condition))
        } else if (isInAndBlock) {
            andConditions.add(mapOf(name to condition))
        } else {
            map[name] = condition
        }
    }

    fun and(block: QueryBuilder.() -> Unit) {
        val wasInAndBlock = isInAndBlock
        isInAndBlock = true
        try {
            block()
        } finally {
            isInAndBlock = wasInAndBlock
        }
    }

    fun or(block: QueryBuilder.() -> Unit) {
        val wasInOrBlock = isInOrBlock
        isInOrBlock = true
        try {
            block()
        } finally {
            isInOrBlock = wasInOrBlock
        }
    }

    fun build(): Map<String, Any> {
        val result = map.toMutableMap()
        if (orConditions.isNotEmpty()) {
            result["\$or"] = orConditions.toList()
        }
        if (andConditions.isNotEmpty()) {
            result["\$and"] = andConditions.toList()
        }
        return result
    }
}


class OperatorBuilder {
    private val map = mutableMapOf<String, Any>()

    fun gt(value: Any) {
        map["\$gt"] = value
    }

    fun gte(value: Any) {
        map["\$gte"] = value
    }

    fun lt(value: Any) {
        map["\$lt"] = value
    }

    fun lte(value: Any) {
        map["\$lte"] = value
    }

    fun between(start: Any, end: Any) {
        map["\$between"] = listOf(start, end)
    }

    fun like(value: Any) {
        map["\$like"] = "%$value%"
    }

    fun notEqual(value: Any) {
        map["\$notEqual"] = value
    }

    fun notNull() {
        map["\$notNull"] = true
    }

    fun isNull() {
        map["\$isNull"] = true
    }

    fun `in`(value: List<Any>) {
        map["\$in"] = value
    }
    // 其他操作符...

    fun build(): Map<String, Any> = map
}

fun json(block: QueryBuilder.() -> Unit): Map<String, Any> {
    return QueryBuilder().apply(block).build()
}