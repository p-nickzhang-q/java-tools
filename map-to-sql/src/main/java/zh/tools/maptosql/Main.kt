package zh.tools.maptosql

import java.util.*

@Table(name = "users")
data class User(
    @Column val id: Int,
    @Column val name: String,
    @Column val age: Int,  // 显式指定列名
    @Column val birthDate: Date,
    @Column val role: String,
    @Column val status: String,
    @Column val isSuperuser: Boolean,
    @Column val country: String,
)

fun main() {
    // 1. 配置实体映射
//    val entityMappings = mapOf(
//        "User" to EntityMapping(
//            tableName = "users",
//            fieldMappings = mapOf(
//                "name" to "name",
//                "age" to "age"
//            )
//        )
//    )

    val entityMappings = AnnotationParser.parseMappings(User::class.java)

    // 2. 创建转换器实例(选择MySQL方言)
    val converter = JsonToSqlConverter(MySqlDialect(), entityMappings)

    // 3. 转换JSON到SQL
    val query = json {
        field("name") {
            like("John")
        }
        field("age") {
            gte(30)
            lt(40)
        }
    }

    val query2 = json {
        field("name") {
            notNull()
            notEqual("zh")
            `in`(listOf("zh2", "zh3"))
        }
        field("age") {
            isNull()
        }
        field("isSuperuser", true)
    }

    val query3 = json {
        field("country", "US")
        or {
            field("country", "US")
            field("country", "CHINA")
            field("age") {
                between(10, 20)
            }
            field("age") {
                lt(10)
            }
        }
    }

    val sql = converter.convertToSelectFromJson("User", query)
    val sql2 = converter.convertToSelectFromJson("User", query2)
    val query4 = mapOf(
        "\$or" to listOf(
            mapOf("age" to mapOf("\$gt" to 20)),
            mapOf(
                "age" to mapOf("\$lt" to 10)
            )
        )
    )
    val sql3 = converter.convertToSelectFromJson("User", query3)

    println("Generated SQL1: $sql")
    println("Generated SQL2: $sql2")
    println("Generated SQL3: $sql3")

//    // 4. 使用PostgreSQL方言
//    val pgConverter = JsonToSqlConverter(PostgreSqlDialect(), entityMappings)
//    val pgSql = pgConverter.convertToSelectFromJson("User", query)
//    println("PostgreSQL SQL: $pgSql")
}