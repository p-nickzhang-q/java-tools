package zh.tools.jpa.filterparse;

import lombok.Data;
import zh.tools.jpa.enums.SpecOperator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class FilterParser {

    private final Path<?> root;
    private final CriteriaBuilder criteriaBuilder;
    private final List<Predicate> restrictions = new ArrayList<>();

    public FilterParser(Path<?> root, CriteriaBuilder criteriaBuilder) {
        this.root = root;
        this.criteriaBuilder = criteriaBuilder;
    }

    public void parseFilter(Map<String, Object> filter) {
        filter.forEach((key, value) -> {
            if (value instanceof List) {
                SpecOperator.List.getParseInstanceAndParse(key,
                        value,
                        this);
            } else if (value instanceof Map) {
                SpecOperator.Map.getParseInstanceAndParse(key,
                        value,
                        this);
            } else {
                SpecOperator.Base.getParseInstanceAndParse(key,
                        value,
                        this);
            }
        });
    }


    @FunctionalInterface
    public interface CreateRestriction {
        Predicate run(Path<?> root, CriteriaBuilder criteriaBuilder);
    }

    public void addRestriction(CreateRestriction createRestriction) {
        restrictions.add(createRestriction.run(root,
                criteriaBuilder));
    }

    public Object ifEnumThenTransformValue(String k, Object cv) {
        if (root
                .get(k)
                .getJavaType()
                .isEnum()) {
            Class javaType = root
                    .get(k)
                    .getJavaType();
            cv = Enum.valueOf(javaType,
                    cv.toString());
        }
        return cv;
    }

    public FilterParser cloneWithRootAndCriteriaBuilder() {
        return new FilterParser(root,
                criteriaBuilder);
    }

    public FilterParser cloneWithCriteriaBuilderAndSetRoot(Path<?> root) {
        return new FilterParser(root,
                criteriaBuilder);
    }

    public Predicate[] getRestrictionsArray() {
        return restrictions.toArray(new Predicate[0]);
    }

}
