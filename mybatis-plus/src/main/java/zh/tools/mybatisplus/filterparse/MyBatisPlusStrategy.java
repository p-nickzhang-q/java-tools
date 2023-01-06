package zh.tools.mybatisplus.filterparse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import zh.tools.common.filterparse.ParseStrategy;

@Getter
public class MyBatisPlusStrategy implements ParseStrategy {
    private final QueryWrapper<?> queryWrapper;

    public MyBatisPlusStrategy(QueryWrapper<?> queryWrapper) {
        this.queryWrapper = queryWrapper;
    }
}
