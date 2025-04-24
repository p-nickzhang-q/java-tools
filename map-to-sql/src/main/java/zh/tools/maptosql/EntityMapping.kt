package zh.tools.maptosql

// 3. 定义实体映射配置
data class EntityMapping(
    val tableName: String,
    val fieldMappings: Map<String, String> = emptyMap(),
    val defaultSelectFields: String = "*"
)

// 类注解：标记表名
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(val name: String)

// 字段注解：标记列名（可选，默认用字段名）
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(val name: String = "")

object AnnotationParser {
    // 解析单个类的映射配置
    private fun parseEntityMapping(clazz: Class<*>): EntityMapping {
        // 获取表名：优先使用@Table注解，没有则使用类名驼峰转蛇形
        val tableName = clazz.getAnnotation(Table::class.java)?.name
            ?: clazz.simpleName.camelToSnakeCase()

        val fieldMappings = clazz.declaredFields
            .associate { field ->
                // 获取列名：优先使用@Column注解，没有则使用字段名驼峰转蛇形
                val columnName = field.getAnnotation(Column::class.java)?.name?.takeIf { it.isNotEmpty() }
                    ?: field.name.camelToSnakeCase()

                field.name to columnName  // 实体字段名 -> 数据库列名
            }

        return EntityMapping(
            tableName = tableName,
            fieldMappings = fieldMappings
        )
    }

    // 批量解析多个类（保持不变）
    fun parseMappings(vararg entityClasses: Class<*>): Map<String, EntityMapping> {
        return entityClasses.associate { clazz ->
            clazz.simpleName to parseEntityMapping(clazz)
        }
    }

    // 驼峰转蛇形辅助函数
    private fun String.camelToSnakeCase(): String =
        fold(StringBuilder()) { acc, c ->
            if (c.isUpperCase()) {
                if (acc.isNotEmpty()) acc.append('_')
                acc.append(c.lowercaseChar())
            } else {
                acc.append(c)
            }
        }.toString()
}