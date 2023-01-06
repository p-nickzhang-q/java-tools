package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.SpecCompare;
import zh.tools.jpa.filterparse.FilterParser;

import javax.persistence.criteria.CriteriaBuilder;

public class GreaterThanParse extends CompareParse {
    public GreaterThanParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        CriteriaBuilder criteriaBuilder = filterParser.getCriteriaBuilder();
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::greaterThan,
                        criteriaBuilder::greaterThan));
    }
}
