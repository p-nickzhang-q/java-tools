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


public class JpaTest extends DataTest {

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
        logInfo(userService
                .filterOne(FilterMap
                        .newFilterMap()
                        .set("name",
                                "zhang san")));
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

    @Test
    public void testNullAndNotNull() {
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("birthDate",
                        Operator.N.getOperator())));
        logInfo(userService
                .filter(FilterMap
                        .newFilterMap()
                        .set("birthDate",
                                Operator.NN.getOperator())));
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

    @Test
    public void testChildEntity2() {
        logInfo(userService.filter(FilterMap
                .newFilterMap().set("department.name", "IT")));
    }
}
