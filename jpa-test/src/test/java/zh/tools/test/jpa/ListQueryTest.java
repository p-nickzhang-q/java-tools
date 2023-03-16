package zh.tools.test.jpa;

import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.Test;
import zh.tools.common.list.ListQuery;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ListQueryTest {


    List<User> users;

    public void init() {
        Department it = new Department("IT");
        Department hr = new Department("HR");
        users = Arrays.asList(new User("zhang san",
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
    }

    @Test
    public void test() {
        init();
        ListQuery<User> query = new ListQuery<>(users);
        Console.log(query.eq(User::getName, "zhang san").or().eq(User::getName, "Li si").result());
        Console.log(query.eq(user -> user.getDepartment().getName(), "HR").result());
        Console.log(query.like(User::getName, "zhang").result());
    }
}
