package zh.tools.mybatisplus.filterparse.impl;

import cn.hutool.core.util.StrUtil;
import zh.tools.mybatisplus.enums.Operator;
import zh.tools.mybatisplus.filterparse.MyBatisPlusFilterParse;
import zh.tools.mybatisplus.filterparse.MyBatisPlusStrategy;

import java.util.Optional;

public class BaseParseMyBatisPlus extends MyBatisPlusFilterParse {

    public BaseParseMyBatisPlus(MyBatisPlusStrategy myBatisPlusStrategy) {
        super(myBatisPlusStrategy);
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
                                    getParseStrategy());
                } else {
                    if (isFuzzy) {
                        Operator.Fuzzy.getParseInstanceAndParse(field,
                                valueString,
                                getParseStrategy());
                    } else {
                        Operator.EQ.getParseInstanceAndParse(field,
                                valueString,
                                getParseStrategy());
                    }
                }
            }
        } else {
            Operator.EQ.getParseInstanceAndParse(field,
                    value,
                    getParseStrategy());
        }
    }
}
