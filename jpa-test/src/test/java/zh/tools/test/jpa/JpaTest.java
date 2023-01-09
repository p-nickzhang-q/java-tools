package zh.tools.test.jpa;

import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zh.tools.common.filterparse.enums.Operator;
import zh.tools.common.map.FilterMap;

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
                        filterMap -> filterMap.set(Operator.LT.getOperator(),
                                "id")));
        logInfo(users);
    }

    @Test
    public void testGreaterThan() {
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(Operator.GT.getOperator(),
                                10)));
        logInfo(users);
    }

    @Test
    public void testBetween() {
        List<User> users = userService.filter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(Operator.BETWEEN.getOperator(),
                                Arrays.asList(12,
                                        20))));
        logInfo(users);
    }

    @Test
    public void testEqualAndNotEqual() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        null)));
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("name",
                                "zhang san"))
                .ifPresent(this::logInfo);
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("age",
                                18))
                .ifPresent(this::logInfo);
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .wrapper("name",
                        map -> map.set(Operator.NE.getOperator(),
                                "zhang san"))));
    }

    private void logInfo(Object object) {
        Console.log("打印信息:");
        Console.log(object);
    }

    @Test
    public void testNullAndNotNull() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("birthDate",
                        Operator.N.getOperator())));
        userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("birthDate",
                                Operator.NN.getOperator()))
                .ifPresent(this::logInfo);
    }

    @Test
    public void testFuzzy() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        String.format("%%%s%%",
                                "si"))));
    }

    @Test
    public void testOr() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .wrapper(Operator.OR.getOperator(),
                        map -> {
                            map
                                    .set("age",
                                            18)
                                    .set("name",
                                            "zhang san");
                        })));
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .wrapList(Operator.OR.getOperator(),
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
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        Arrays.asList("zhang san",
                                "Li si"))));
    }

    @Test
    public void testChildEntity() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .wrapper("department",
                        map -> {
                            map.set("name",
                                    "IT");
                        })));
    }
}
