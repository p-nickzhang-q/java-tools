package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.mybatisplus.enums.Operator;
import zh.tools.mybatisplus.filterparse.MyBatisPlusFilterParse;
import zh.tools.mybatisplus.filterparse.MyBatisPlusStrategy;

import java.util.List;
import java.util.Optional;

public class ListParse extends MyBatisPlusFilterParse {

    public ListParse(MyBatisPlusStrategy myBatisPlusStrategy) {
        super(myBatisPlusStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        List<Object> values = (List<Object>) value;
        Optional<Operator> operatorOptional = Operator.getByOperator(field);
        if (operatorOptional.isPresent()) {
            Operator operator = operatorOptional.get();

        }
    }
}
