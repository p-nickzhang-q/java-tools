package zh.tools.jpa.enums;

import cn.hutool.core.bean.DynaBean;
import lombok.Getter;
import zh.tools.jpa.FilterParser;
import zh.tools.jpa.filterparse.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
public enum SpecOperator {
    START("$start",
            GreaterEqualParse.class), GE("$ge",
            GreaterEqualParse.class), GT("$gt",
            GreaterThanParse.class), LT("$lt",
            LessThanParse.class), END("$end",
            LessEqualParse.class), LE("$le",
            LessEqualParse.class), BETWEEN("$between",
            BetweenParse.class), NE("$ne",
            NotEqualParse.class), EQ("$eq",
            EqualParse.class), OR("$or"), AND("$and"), N("$n",
            NullParse.class), NN("$nn",
            NotNullParse.class), Fuzzy(FuzzyParse.class), List(ListParse.class), Map(MapParse.class), Base(BaseParse.class),
    ;

    private String operator;
    private Class<?> parseClass;

    SpecOperator(String operator) {
        this.operator = operator;
    }

    SpecOperator(Class<?> parseClass) {
        this.parseClass = parseClass;
    }

    SpecOperator(String operator, Class<?> parseClass) {
        this.operator = operator;
        this.parseClass = parseClass;
    }

    public static Optional<SpecOperator> getByOperator(String operator) {
        return Arrays
                .stream(SpecOperator.values())
                .filter(specOperator -> Objects.equals(specOperator.getOperator(),
                        operator))
                .findFirst();
    }

    public void getParseInstanceAndParse(String key, Object childValue, FilterParser filterParser) {
        DynaBean dynaBean = DynaBean.create(getParseClass(),
                filterParser);
        dynaBean.invoke("parse",
                key,
                childValue);
    }

    public void getParseInstanceAndParse(String key, FilterParser filterParser) {
        DynaBean dynaBean = DynaBean.create(getParseClass(),
                filterParser);
        dynaBean.invoke("parse",
                key);
    }

    public boolean isAndOr() {
        return is(OR) || is(AND);
    }

    public boolean is(SpecOperator operator) {
        return this.equals(operator);
    }


}
