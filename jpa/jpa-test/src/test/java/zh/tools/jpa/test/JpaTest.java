package zh.tools.jpa.test;

import cn.hutool.core.lang.Console;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import zh.tools.common.consts.SpecOperator;
import zh.tools.common.map.FilterMap;

import java.util.List;

@SpringBootTest()
@RunWith(SpringRunner.class)
public class JpaTest {

    @Autowired
    private UserService userService;

    @Test
    public void filterParserTest() {
        User user = new User("zh",
                12);
        userService
                .repository()
                .save(user);
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        stringObjectFilterMap -> stringObjectFilterMap.set(SpecOperator.LE,
                                13)));
        Console.log(users);
        userService
                .repository()
                .deleteById(user.getId());
    }
}
