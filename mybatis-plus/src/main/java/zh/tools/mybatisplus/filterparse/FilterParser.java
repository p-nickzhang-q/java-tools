package zh.tools.mybatisplus.filterparse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import zh.tools.common.map.FilterMap;
import zh.tools.mybatisplus.enums.Operator;

import java.util.List;
import java.util.Map;

public class FilterParser<Entity> {
    private final QueryWrapper<Entity> queryWrapper = new QueryWrapper<>();

    public QueryWrapper<Entity> parseFilter(FilterMap<String, Object> filter) {
        MyBatisPlusStrategy myBatisPlusStrategy = new MyBatisPlusStrategy(queryWrapper);
        filter.forEach((key, value) -> {
            String dataBaseField = MyBatisPlusFilterParse.humpToUnderline(key);
            if (value instanceof List) {
                Operator.List.getParseInstanceAndParse(dataBaseField,
                        value,
                        myBatisPlusStrategy);
            } else if (value instanceof Map) {
                Operator.Map.getParseInstanceAndParse(dataBaseField,
                        value,
                        myBatisPlusStrategy);
            } else {
                Operator.Base.getParseInstanceAndParse(dataBaseField,
                        value,
                        myBatisPlusStrategy);
            }
        });
        return queryWrapper;
    }
}
