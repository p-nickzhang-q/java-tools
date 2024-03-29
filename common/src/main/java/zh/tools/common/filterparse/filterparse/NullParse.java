package zh.tools.common.filterparse.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

public class NullParse extends BaseFilterParse {
    public NullParse(ParseStrategy parseStrategy) {
        super(parseStrategy);
    }

    @Override
    public void parse(String field) {
        parseStrategy.nullProcess(field);
    }
}
