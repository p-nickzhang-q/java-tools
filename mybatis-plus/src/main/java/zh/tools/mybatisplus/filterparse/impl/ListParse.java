package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.mybatisplus.enums.Operator;

import java.util.List;
import java.util.Optional;

public class ListParse extends BaseFilterParse {


    public ListParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        List<Object> values = (List<Object>) value;
        Optional<Operator> operatorOptional = Operator.getByOperator(field);
        if (operatorOptional.isPresent()) {
            Operator operator = operatorOptional.get();
            if (operator.is(Operator.AND)) {
                parseStrategy.and(values);
            } else if (operator.is(Operator.OR)) {
                parseStrategy.or(values);
            }
        } else {
            parseStrategy.in(field,
                    values);
        }
    }
}
