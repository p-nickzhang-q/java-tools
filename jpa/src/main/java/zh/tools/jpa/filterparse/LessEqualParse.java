package zh.tools.jpa.filterparse;

import zh.tools.jpa.FilterParser;
import zh.tools.jpa.SpecCompare;

import javax.persistence.criteria.CriteriaBuilder;

public class LessEqualParse extends CompareParse {
    public LessEqualParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        CriteriaBuilder criteriaBuilder = filterParser.getCriteriaBuilder();
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::lessThanOrEqualTo,
                        criteriaBuilder::lessThanOrEqualTo));
    }
}
