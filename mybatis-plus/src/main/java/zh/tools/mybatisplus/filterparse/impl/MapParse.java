package zh.tools.mybatisplus.filterparse.impl;

import cn.hutool.core.exceptions.ValidateException;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.mybatisplus.enums.Operator;

import java.util.Map;
import java.util.Optional;

public class MapParse extends BaseParse {
    public MapParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object o) {
        Map<String, Object> value = (Map<String, Object>) o;
        Optional<Operator> operatorOptional = Operator.getByOperator(field);
        if (operatorOptional.isPresent()) {
            Operator operator = operatorOptional.get();
            if (operator.is(Operator.OR)) {
                parseStrategy.or(value);
            } else if (operator.is(Operator.AND)) {
                parseStrategy.and(value);
            }
        } else {
            value.forEach((childKey, childValue) -> {
                Optional<Operator> childOperatorOptional = Operator.getByOperator(childKey);
                if (childOperatorOptional.isPresent()) {
                    childOperatorOptional
                            .get()
                            .getParseInstanceAndParse(field,
                                    childValue,
                                    parseStrategy);
                } else {
                    // todo 子对象处理
                    throw new ValidateException(String.format("不支持操作符[%s]",
                            childKey));
                }
            });
        }
    }
}
