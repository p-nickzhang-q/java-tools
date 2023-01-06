package zh.tools.jpa.filterparse.impl;

import cn.hutool.core.util.StrUtil;
import zh.tools.jpa.enums.SpecOperator;
import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

import java.util.Optional;

public class BaseParse extends FilterParse {
    public BaseParse(FilterParser filterParser) {
        super(filterParser);
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
                Optional<SpecOperator> operatorOptional = SpecOperator.getByOperator(valueString);
                if (operatorOptional.isPresent()) {
                    operatorOptional
                            .get()
                            .getParseInstanceAndParse(field,
                                    filterParser);
                } else {
                    if (isFuzzy) {
                        SpecOperator.Fuzzy.getParseInstanceAndParse(field,
                                valueString,
                                filterParser);
                    } else {
                        SpecOperator.EQ.getParseInstanceAndParse(field,
                                valueString,
                                filterParser);
                    }
                }
            }
        } else {
            SpecOperator.EQ.getParseInstanceAndParse(field,
                    value,
                    filterParser);
        }
    }
}
