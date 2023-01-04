package zh.tools.jpa.filterparse;

import cn.hutool.core.exceptions.ValidateException;
import lombok.Getter;
import zh.tools.jpa.FilterParser;

@Getter
public class FilterParse {
    protected final FilterParser filterParser;

    public FilterParse(FilterParser filterParser) {
        this.filterParser = filterParser;
    }

    public void parse(String field, Object value) {
        throw new ValidateException("未实现FilterParse逻辑");
    }

    public void parse(String field) {
        throw new ValidateException("未实现FilterParse逻辑");
    }

}
