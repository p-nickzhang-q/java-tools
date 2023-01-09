package zh.tools.mybatisplus.filterparse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import zh.tools.common.map.FilterMap;

public class FilterParser<Entity> {
    private final QueryWrapper<Entity> queryWrapper = new QueryWrapper<>();

    public QueryWrapper<Entity> parseFilter(FilterMap<String, Object> filter) {
        MyBatisPlusStrategy myBatisPlusStrategy = new MyBatisPlusStrategy(queryWrapper);
        myBatisPlusStrategy.parseFilter(filter);
        return queryWrapper;
    }
}
