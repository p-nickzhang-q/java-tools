package zh.tools.common.booleans;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.lang.Console;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class Where {

    @Getter
    private enum Logic {
        And((aBoolean, aBoolean2) -> aBoolean && aBoolean2), Or((aBoolean, aBoolean2) -> aBoolean || aBoolean2);

        private final BiPredicate<Boolean, Boolean> func;

        Logic(BiPredicate<Boolean, Boolean> func) {
            this.func = func;
        }
    }

    @Data
    @Accessors(chain = true)
    private static class Result {
        private final Boolean b;
        private Logic logic;

        public Result(Boolean b) {
            this.b = b;
        }
    }

    @Getter
    private static class Wrapper {
        Where where = new Where();
    }

    private Result current;
    private final List<Result> results = new ArrayList<>();

    public Where eq(Object o1, Object o2) {
        current = new Result(Objects.equals(o1,
                o2));
        return this;
    }

    public Where and() {
        if (current == null) {
            throw new ValidateException("不能连续使用and/or");
        }
        current.setLogic(Logic.And);
        results.add(current);
        current = null;
        return this;
    }

    public Where or() {
        if (current == null) {
            throw new ValidateException("不能连续使用and/or");
        }
        current.setLogic(Logic.Or);
        results.add(current);
        current = null;
        return this;
    }

    public Where wrapper(Consumer<Where> consumer) {
        Wrapper wrapper = new Wrapper();
        consumer.accept(wrapper.getWhere());
        current = new Result(wrapper
                .getWhere()
                .result());
        return this;
    }

    public Boolean result() {
        Boolean finalResult = current.getB();
        if (!results.isEmpty()) {
            for (Result result : results) {
                finalResult = result
                        .getLogic()
                        .getFunc()
                        .test(finalResult,
                                result.getB());
            }
        }
        return finalResult;
    }

    public static void main(String[] args) {
//        Console.log(new Where().eq(1, 2).or().eq(1, 1).result());
        int input = 3;
        String input2 = "zh2";
        Where zh3 = new Where()
                .wrapper(where -> where
                        .eq(input,
                                1)
                        .or()
                        .eq(input,
                                2))
                .or()
                .eq(input2,
                        "zh3");
        Console.log(zh3.result());
    }
}
