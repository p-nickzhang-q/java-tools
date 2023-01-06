package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

public class NullParse extends FilterParse {
    public NullParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field) {
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.isNull(root.get(field)));
    }
}
