package zh.tools.test.mybatisplus

import com.baomidou.mybatisplus.annotation.DbType
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import zh.tools.mybatisplus.SqlService

@SpringBootApplication
@MapperScan("zh.tools.test.mybatisplus")
open class App {
    /*分页配置*/
    @Bean
    open fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        val interceptor = MybatisPlusInterceptor()
        val paginationInnerInterceptor = PaginationInnerInterceptor()
        paginationInnerInterceptor.dbType = DbType.MYSQL
        paginationInnerInterceptor.isOverflow = true
        interceptor.addInnerInterceptor(paginationInnerInterceptor)
        return interceptor
    }

    @Bean
    open fun sqlQueryHelperV2(
        userService: UserService,
    ): SqlService<UserMapper, User> {
        return SqlService(jsonToSqlConverter, userService, "User")
    }

    fun main(args: Array<String>) {
        SpringApplication.run(
            App::class.java,
            *args
        )
    }
}