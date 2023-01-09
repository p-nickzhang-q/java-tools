package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.common.filterparse.enums.Operator;

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
                    parseStrategy.childEntityProcess(field,
                            value);
                }
            });
        }
    }
}
