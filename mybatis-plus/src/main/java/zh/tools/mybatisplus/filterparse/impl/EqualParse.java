package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class EqualParse extends BaseFilterParse {
    public EqualParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.equal(field,
                value);
    }
}
