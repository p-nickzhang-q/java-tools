package zh.tools.common.filterparse;

import cn.hutool.core.exceptions.ValidateException;

public class BaseFilterParse {

    protected ParseStrategy parseStrategy;

    public BaseFilterParse(ParseStrategy parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public void parse(String field, Object value) {
        throw new ValidateException("未实现FilterParse逻辑");
    }

    public void parse(String field) {
        throw new ValidateException("未实现FilterParse逻辑");
    }
}
