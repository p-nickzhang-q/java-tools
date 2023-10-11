package zh.tools.test.jpa;

import cn.hutool.core.lang.Console;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class SpringUtilTest {

    public enum Type {
        Type1Service {
            @Autowired
            private UserService userService;

            @Override
            public User getUser() {
                return userService.filter().get(0);
            }
        };

        abstract public User getUser();
    }

    @Test
    public void test1() {
        UserService bean = SpringUtil.getBean(UserService.class);
        Console.log(bean);
//        Type bean = SpringUtil.getBean(Type.Type1Service.getClass());
//        User user = bean.getUser();
//        Console.log(user);
    }

}
