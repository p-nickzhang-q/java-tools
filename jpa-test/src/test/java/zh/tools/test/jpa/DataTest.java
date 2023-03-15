package zh.tools.test.jpa;

import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataTest {
    @Autowired
    protected UserService userService;
    @Autowired
    private DepartmentService departmentService;
    protected List<User> users;
    protected List<Department> departments;

    @BeforeAll
    public void init() {
        Department it = new Department("IT");
        Department hr = new Department("HR");
        departments = Arrays.asList(it,
                hr);
        departmentService
                .repository()
                .saveAll(departments);

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
        userService
                .repository()
                .saveAll(users);
    }

    @AfterAll
    public void finish() {
        userService
                .repository()
                .deleteAll(users);
        departmentService
                .repository()
                .deleteAll(departments);
    }

    protected void logInfo(Object object) {
        Console.log("打印信息:");
        Console.log(object);
    }
}
