package zh.tools.jpa.filterparse;

import cn.hutool.core.util.StrUtil;
import zh.tools.jpa.FilterParser;
import zh.tools.jpa.enums.SpecOperator;

import java.util.Optional;

public class BaseParse extends FilterParse {
    public BaseParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        boolean isFuzzy = value
                .toString()
                .contains("%");
        if (value instanceof String) {
            if (StrUtil.isNotBlank(value.toString())) {
                Optional<SpecOperator> operatorOptional = SpecOperator.getByOperator(value.toString());
                if (operatorOptional.isPresent()) {
                    operatorOptional
                            .get()
                            .getParseInstanceAndParse(field,
                                    filterParser);
                } else {
                    if (isFuzzy) {
                        SpecOperator.Fuzzy.getParseInstanceAndParse(field,
                                value,
                                filterParser);
                    } else {
                        SpecOperator.EQ.getParseInstanceAndParse(field,
                                value,
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
