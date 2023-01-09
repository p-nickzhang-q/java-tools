package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class LessEqualParse extends BaseFilterParse {
    public LessEqualParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        parseStrategy.lessEqual(field,
                value);
    }
}
