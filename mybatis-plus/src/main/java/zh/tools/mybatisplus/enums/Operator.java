package zh.tools.mybatisplus.enums;

import cn.hutool.core.bean.DynaBean;
import lombok.Getter;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.mybatisplus.filterparse.impl.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
public enum Operator {
    START("$start",
            StartParse.class), GE("$ge",
            GreaterEqualParse.class), GT("$gt",
            GreaterThanParse.class), LT("$lt",
            LessThanParse.class), END("$end",
            EndParse.class), LE("$le",
            LessEqualParse.class), BETWEEN("$between",
            BetweenParse.class), NE("$ne",
            NotEqualParse.class), EQ("$eq",
            EqualParse.class), OR("$or"), AND("$and"), N("$n",
            NullParse.class), NN("$nn",
            NotNullParse.class), Fuzzy(FuzzyParse.class), List(ListParse.class), Map(MapParse.class), Base(BaseParse.class),
    ;

    private String operator;
    private Class<?> parseClass;

    Operator(String operator) {
        this.operator = operator;
    }

    Operator(Class<?> parseClass) {
        this.parseClass = parseClass;
    }

    Operator(String operator, Class<?> parseClass) {
        this.operator = operator;
        this.parseClass = parseClass;
    }

    public static Optional<Operator> getByOperator(String operator) {
        return Arrays
                .stream(Operator.values())
                .filter(specOperator -> Objects.equals(specOperator.getOperator(),
                        operator))
                .findFirst();
    }

    public void getParseInstanceAndParse(String key, Object childValue, ParseStrategy parseStrategy) {
        DynaBean dynaBean = DynaBean.create(getParseClass(),
                parseStrategy);
        dynaBean.invoke("parse",
                key,
                childValue);
    }

    public void getParseInstanceAndParse(String key, ParseStrategy parseStrategy) {
        DynaBean dynaBean = DynaBean.create(getParseClass(),
                parseStrategy);
        dynaBean.invoke("parse",
                key);
    }

    public boolean isAndOr() {
        return is(OR) || is(AND);
    }

    public boolean is(Operator operator) {
        return this.equals(operator);
    }
}
