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
        val tableAnnotation = clazz.getAnnotation(Table::class.java)
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} must have @Table annotation")

        val fieldMappings = clazz.declaredFields
            .associate { field ->
                val columnAnnotation = field.getAnnotation(Column::class.java)
                val columnName = columnAnnotation?.name?.takeIf { it.isNotEmpty() } ?: field.name.camelToSnakeCase()
                field.name to columnName  // 实体字段名 -> 数据库列名
            }

        return EntityMapping(
            tableName = tableAnnotation.name,
            fieldMappings = fieldMappings
        )
    }

    // 批量解析多个类
    fun parseMappings(vararg entityClasses: Class<*>): Map<String, EntityMapping> {
        return entityClasses.associate { clazz ->
            clazz.simpleName to parseEntityMapping(clazz)
        }
    }

    private fun String.camelToSnakeCase(): String =
        fold(StringBuilder()) { acc, c ->
            if (c.isUpperCase()) {
                acc.append('_').append(c.lowercaseChar())
            } else {
                acc.append(c)
            }
        }.toString()

}