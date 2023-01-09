package zh.tools.mybatisplus.filterparse.impl;

import cn.hutool.core.util.StrUtil;
import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.mybatisplus.enums.Operator;

import java.util.Optional;

public class BaseParse extends BaseFilterParse {


    public BaseParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        if (value == null) {
            return;
        }
        boolean isString = value instanceof String;
        if (isString) {
            String valueString = value.toString();
            boolean isFuzzy = valueString.contains("%");
            if (StrUtil.isNotBlank(valueString)) {
                Optional<Operator> operatorOptional = Operator.getByOperator(valueString);
                if (operatorOptional.isPresent()) {
                    operatorOptional
                            .get()
                            .getParseInstanceAndParse(field,
                                    parseStrategy);
                } else {
                    if (isFuzzy) {
                        Operator.Fuzzy.getParseInstanceAndParse(field,
                                valueString,
                                parseStrategy);
                    } else {
                        Operator.EQ.getParseInstanceAndParse(field,
                                valueString,
                                parseStrategy);
                    }
                }
            }
        } else {
            Operator.EQ.getParseInstanceAndParse(field,
                    value,
                    parseStrategy);
        }
    }
}
