package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class LessThanParse extends BaseFilterParse {
    public LessThanParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.lessThan(field,
                value);
    }
}
