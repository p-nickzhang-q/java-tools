package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

public class NotEqualParse extends FilterParse {
    public NotEqualParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        value = filterParser.ifEnumThenTransformValue(field,
                value);
        Object finalValue = value;
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(field),
                finalValue));
    }
}
