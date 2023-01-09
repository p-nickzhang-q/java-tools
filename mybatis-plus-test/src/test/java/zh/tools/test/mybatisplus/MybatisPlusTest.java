package zh.tools.test.mybatisplus;

import cn.hutool.core.lang.Console;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zh.tools.common.map.FilterMap;
import zh.tools.common.paging.FilterRequest;
import zh.tools.mybatisplus.enums.Operator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MybatisPlusTest {

    @Autowired
    private UserService userService;
    private List<User> users;

    @BeforeAll
    public void init() {
        users = Arrays.asList(new User("zhang san",
                        12),
                new User("Li si",
                        18,
                        LocalDate.now()));
        userService.saveOrUpdateBatch(users);
    }

    @AfterAll
    public void finish() {
        List<Long> ids = users
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
        userService.removeByIds(ids);
    }

    private void logInfo(Object object) {
        Console.log("打印信息:");
        Console.log(object);
    }

    @Test
    public void testGreaterThan() {
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setFilter(FilterMap
                .newFilterMap()
                .wrapper("age",
                        filterMap -> filterMap.set(Operator.GT.getOperator(),
                                10)));
        IPage<User> users = userService.filter(filterRequest);
        logInfo(users.getTotal());
        logInfo(users.getRecords());
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
        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("name",
                        "zhang san")));

        logInfo(userService.filter(FilterMap
                .newFilterMap()
                .set("age",
                        18)));
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
        logInfo(userService.filter(FilterMap
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
}
