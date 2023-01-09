package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class FuzzyParse extends BaseFilterParse {

    public FuzzyParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.fuzzy(field,
                value);
    }
}
