package zh.tools.jpa.filterparse;

import zh.tools.jpa.FilterParser;

public class NullParse extends FilterParse {
    public NullParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field) {
        filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.isNull(root.get(field)));
    }
}
