package zh.tools.jpa.test;

import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zh.tools.common.map.FilterMap;
import zh.tools.jpa.enums.SpecOperator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JpaTest {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    private List<User> users;
    private List<Department> departments;

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

    @Test
    public void testLessThanPath() {
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(SpecOperator.LT.getOperator(),
                                "id")));
        Console.log(users);
    }

    @Test
    public void testGreaterThan() {
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(SpecOperator.GT.getOperator(),
                                10)));
        Console.log(users);
    }

    @Test
    public void testBetween() {
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(SpecOperator.BETWEEN.getOperator(),
                                Arrays.asList(12,
                                        20))));
        Console.log(users);
    }

    @Test
    public void testEqualAndNotEqual() {
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("name",
                                "zhang san"))
                .ifPresent(Console::log);
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("age",
                                18))
                .ifPresent(Console::log);
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .wrapper("name",
                        map -> map.set(SpecOperator.NE.getOperator(),
                                "zhang san"))));
    }

    @Test
    public void testNullAndNotNull() {
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .set("birthDate",
                        SpecOperator.N.getOperator())));
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("birthDate",
                                SpecOperator.NN.getOperator()))
                .ifPresent(Console::log);
    }

    @Test
    public void testFuzzy() {
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        String.format("%%%s%%",
                                "si"))));
    }

    @Test
    public void testOr() {
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .wrapper(SpecOperator.OR.getOperator(),
                        map -> {
                            map
                                    .set("age",
                                            18)
                                    .set("name",
                                            "zhang san");
                        })));
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .wrapList(SpecOperator.OR.getOperator(),
                        list -> {
                            list.add(FilterMap
                                    .newFilterMap()
                                    .set("age",
                                            18));
                            list.add(FilterMap
                                    .newFilterMap()
                                    .set("name",
                                            "zhang san"));
                        })));
    }

    @Test
    public void testList() {
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        Arrays.asList("zhang san",
                                "Li si"))));
    }

    @Test
    public void testChildEntity() {
        Console.log(userService.filter(FilterMap
                .newFilterMap()
                .wrapper("department",
                        map -> {
                            map.set("name",
                                    "IT");
                        })));
    }
}
