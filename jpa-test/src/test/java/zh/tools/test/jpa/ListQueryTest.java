package zh.tools.test.jpa;

import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.Test;
import zh.tools.common.list.ListQuery;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ListQueryTest {
    @Test
    public void test() {
        Department it = new Department("IT");
        Department hr = new Department("HR");
        List<User> users = Arrays.asList(new User("zhang san",
                        12,
                        it),
                new User("Li si",
                        18,
                        it),
                new User("Wang wu",
                        22,
                        hr),
                new User("zhao si",
                        25,
                        LocalDate.now()));
        ListQuery<User> query = new ListQuery<>(users);
        List<User> list = query.eq(User::getName, "zhang san").or().eq(User::getName, "Li si").result();
        Console.log(list);
    }
}
