package zh.tools.jpa.filterparse;

import zh.tools.jpa.FilterParser;
import zh.tools.jpa.enums.SpecOperator;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ListParse extends FilterParse {
    public ListParse(FilterParser filterParser) {
        super(filterParser);
    }

    public void parseFilters(List<?> values, Consumer<FilterParser> andOr) {
        FilterParser andFilterParser = this.filterParser.cloneWithRootAndCriteriaBuilder();
        for (Object value : values) {
            if (value instanceof Map) {
                FilterParser cloneRootAndCriteriaBuilderFilterParser = this.filterParser.cloneWithRootAndCriteriaBuilder();
                cloneRootAndCriteriaBuilderFilterParser.parseFilter(((Map<String, Object>) value));
                andFilterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.and(cloneRootAndCriteriaBuilderFilterParser.getRestrictionsArray()));
            }
        }
        andOr.accept(andFilterParser);
    }

    @Override
    public void parse(String field, Object value) {
        List<Object> values = (List<Object>) value;
        Optional<SpecOperator> operatorOptional = SpecOperator.getByOperator(field);
        operatorOptional.ifPresentOrElse(specOperator -> {
                    if (specOperator.is(SpecOperator.AND)) {
                        parseFilters(values,
                                cloneFilterParser -> {
                                    filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.and(cloneFilterParser.getRestrictionsArray()));
                                });
                    } else if (specOperator.is(SpecOperator.OR)) {
                        parseFilters(values,
                                cloneFilterParser -> {
                                    filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.or(cloneFilterParser.getRestrictionsArray()));
                                });
                    }
                },
                () -> filterParser.addRestriction((root, criteriaBuilder) -> {
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get(field));
                    for (Object cv : values) {
                        cv = filterParser.ifEnumThenTransformValue(field,
                                cv);
                        in.value(cv);
                    }
                    return in;
                }));
    }
}
