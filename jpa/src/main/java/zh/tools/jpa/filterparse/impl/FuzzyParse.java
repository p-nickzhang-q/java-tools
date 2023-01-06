package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

public class FuzzyParse extends FilterParse {
    public FuzzyParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.like(root
                        .get(field)
                        .as(String.class),
                value.toString()));
    }
}
