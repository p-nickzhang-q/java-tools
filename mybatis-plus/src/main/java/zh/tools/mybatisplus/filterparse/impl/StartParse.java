package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class StartParse extends BaseFilterParse {
    public StartParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        if (value != null) {
            parseStrategy.start(field,
                    value.toString());
        }
    }
}
