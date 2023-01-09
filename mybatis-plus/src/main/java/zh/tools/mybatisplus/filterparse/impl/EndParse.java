package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class EndParse extends BaseFilterParse {
    public EndParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        if (value != null) {
            parseStrategy.end(field,
                    value.toString());
        }
    }
}
