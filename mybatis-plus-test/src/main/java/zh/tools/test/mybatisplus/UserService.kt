package zh.tools.test.mybatisplus

import org.springframework.stereotype.Service
import zh.tools.maptosql.JsonToSqlConverter
import zh.tools.maptosql.MySqlDialect
import zh.tools.mybatisplus.CommonService

val jsonToSqlConverter = JsonToSqlConverter(MySqlDialect())

@Service
class UserService : CommonService<UserMapper, User, Long>(
)
