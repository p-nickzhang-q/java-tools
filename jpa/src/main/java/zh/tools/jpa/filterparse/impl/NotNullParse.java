package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

public class NotNullParse extends FilterParse {
    public NotNullParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field) {
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(field)));
    }
}
