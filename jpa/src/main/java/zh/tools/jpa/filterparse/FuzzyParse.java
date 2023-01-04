package zh.tools.jpa.filterparse;

import zh.tools.jpa.FilterParser;

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
