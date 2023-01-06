package zh.tools.jpa.filterparse.impl;

import zh.tools.jpa.enums.SpecOperator;
import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MapParse extends FilterParse {
    public MapParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object o) {
        Map<String, Object> value = (Map<String, Object>) o;
        CriteriaBuilder criteriaBuilder = filterParser.getCriteriaBuilder();
        Path<?> root = filterParser.getRoot();
        List<Predicate> restrictions = filterParser.getRestrictions();
        Optional<SpecOperator> someOperatorOptional = SpecOperator.getByOperator(field);
        if (someOperatorOptional.isPresent() && someOperatorOptional
                .get()
                .isAndOr()) {
            FilterParser cloneRootAndCriteriaBuilderFilterParser = filterParser.cloneWithRootAndCriteriaBuilder();
            cloneRootAndCriteriaBuilderFilterParser.parseFilter(value);
            Predicate[] restrictionsArray = cloneRootAndCriteriaBuilderFilterParser.getRestrictionsArray();
            if (someOperatorOptional
                    .get()
                    .equals(SpecOperator.OR)) {
                restrictions.add(criteriaBuilder.or(restrictionsArray));
            } else {
                restrictions.add(criteriaBuilder.and(restrictionsArray));
            }
        } else {
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String childKey = entry.getKey();
                Object childValue = entry.getValue();
                Optional<SpecOperator> operatorOptional = SpecOperator.getByOperator(childKey);
                if (operatorOptional.isPresent()) {
                    operatorOptional
                            .get()
                            .getParseInstanceAndParse(field,
                                    childValue,
                                    filterParser);
                } else {
                    FilterParser childFilterParser = filterParser.cloneWithCriteriaBuilderAndSetRoot(root.get(field));
                    childFilterParser.parseFilter(value);
                    restrictions.addAll(childFilterParser.getRestrictions());
                }
            }
        }
    }
}
