package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class GreaterEqualParse extends BaseFilterParse {
    public GreaterEqualParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.greaterEqual(field,
                value);
    }
}
