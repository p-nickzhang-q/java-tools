package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class NotEqualParse extends BaseFilterParse {
    public NotEqualParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.notEqual(field,
                value);
    }
}
