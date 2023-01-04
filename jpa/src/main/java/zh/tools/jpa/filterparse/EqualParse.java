package zh.tools.jpa.filterparse;

import zh.tools.jpa.FilterParser;

public class EqualParse extends FilterParse {
    public EqualParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        value = filterParser.ifEnumThenTransformValue(field,
                value);
        Object finalValue = value;
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.equal(root.get(field),
                finalValue));
    }
}
