package zh.tools.test.jpa

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import zh.tools.common.map.FilterMap
import zh.tools.jpa.JpaFilterRequest
import zh.tools.jpa.PersistentEntities
import java.time.LocalDate

@SpringBootTest
class PersistentEntityTest {

    @Test
    fun test1() {
        val user = User("zhang san", 10)
        user.save()
    }

    class Users(override var list: List<User> = mutableListOf()) : PersistentEntities<User, Long> {
        override val clazz = UserService::class.java
    }

    @Test
    fun test2() {
        val users = Users(mutableListOf(User("zh", 1), User("ls", 2)))
        users.save()
        users.remove()
    }

    @Test
    fun test3() {
        val users = Users()
        users.filter(FilterMap.newFilterMap())
        print(users.list)
    }

    @Test
    fun test4() {
        val users = Users()
//        val page = users.filter(JpaFilterRequest())
//        println(users.list)
//        println(page.content)
        val user = users.findById(366).get()
        println(user)
        user.birthDate = LocalDate.now()
        user.save()
    }
}