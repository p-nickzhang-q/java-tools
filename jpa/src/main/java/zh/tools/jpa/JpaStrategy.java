package zh.tools.jpa;

import lombok.Getter;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.common.filterparse.enums.Operator;
import zh.tools.common.time.JavaTimeUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class JpaStrategy implements ParseStrategy {

    public final static String DATE_REG = "\\d{4}-\\d{2}-\\d{2}";
    public static final String DOT = "\\.";

    private final Path<?> root;

    private final CriteriaBuilder criteriaBuilder;
    @Getter
    private final List<Predicate> restrictions = new ArrayList<>();

    public JpaStrategy(Path<?> root, CriteriaBuilder criteriaBuilder) {
        this.root = root;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    public void fuzzy(String field, Object value) {
        restrictions.add(criteriaBuilder.like(getPath(field)
                        .as(String.class),
                value.toString()));
    }

    private Path<?> getPath(String field) {
        Path<?> path = root;
        String[] split = field.split(DOT);
        for (String s : split) {
            path = path.get(s);
        }
        return path;
    }

    @Override
    public void between(String field, Object value) {
        Object[] temp;
        if (value instanceof List) {
            temp = ((List) value).toArray();
        } else {
            temp = (Object[]) value;
        }
        if (temp != null && temp.length > 0) {
            if (temp[0] instanceof String) {
                if (Pattern.matches(DATE_REG,
                        temp[0].toString())) {
                    temp[0] = JavaTimeUtil.str2LocalDate(temp[0].toString());
                    temp[1] = JavaTimeUtil.str2LocalDate(temp[1].toString());
                } else {
                    temp[0] = JavaTimeUtil.str2LocalDateTime(temp[0].toString());
                    temp[1] = JavaTimeUtil.str2LocalDateTime(temp[1].toString());
                }
            }
            restrictions.add(criteriaBuilder.between((Path) getPath(field),
                    (Comparable) temp[0],
                    (Comparable) temp[1]));
        }
    }

    public Object ifEnumThenTransformValue(String k, Object cv) {
        Path<?> path = getPath(k);
        if (path
                .getJavaType()
                .isEnum()) {
            Class javaType = path
                    .getJavaType();
            cv = Enum.valueOf(javaType,
                    cv.toString());
        }
        return cv;
    }

    @Override
    public void equal(String field, Object value) {
        value = ifEnumThenTransformValue(field,
                value);
        restrictions.add(criteriaBuilder.equal(getPath(field),
                value));
    }

    private void processCompare(String field, Object value, SpecCompare compare) {
        Predicate predicate;
        Path<Object> path = (Path<Object>) getPath(field);
        if (value instanceof String) {
            if (JavaTimeUtil.matchDateReg(value.toString())) {
                predicate = compare.commonCompare.compare(path,
                        JavaTimeUtil.str2LocalDate(value.toString()));
            } else if (JavaTimeUtil.matchDateTimeReg(value.toString())) {
                predicate = compare.commonCompare.compare(path,
                        JavaTimeUtil.str2LocalDateTime(value.toString()));
            } else {
                predicate = compare.pathCompare.compare(path,
                        root.get(value.toString()));
            }
        } else {
            predicate = compare.commonCompare.compare(path,
                    ((Comparable) value));
        }
        restrictions.add(predicate);
    }

    @Override
    public void greaterEqual(String field, Object value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::greaterThanOrEqualTo,
                        criteriaBuilder::greaterThanOrEqualTo));
    }

    @Override
    public void greaterThan(String field, Object value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::greaterThan,
                        criteriaBuilder::greaterThan));
    }

    @Override
    public void lessThan(String field, Object value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::lessThan,
                        criteriaBuilder::lessThan));
    }

    @Override
    public void lessEqual(String field, Object value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::lessThanOrEqualTo,
                        criteriaBuilder::lessThanOrEqualTo));
    }

    @Override
    public void start(String field, String value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::greaterThanOrEqualTo,
                        criteriaBuilder::greaterThanOrEqualTo));
    }

    @Override
    public void end(String field, String value) {
        processCompare(field,
                value,
                new SpecCompare(criteriaBuilder::lessThanOrEqualTo,
                        criteriaBuilder::lessThanOrEqualTo));
    }

    @Override
    public void notEqual(String field, Object value) {
        value = ifEnumThenTransformValue(field,
                value);
        restrictions.add(criteriaBuilder.notEqual(getPath(field),
                value));
    }

    @Override
    public void notNull(String field) {
        restrictions.add(criteriaBuilder.isNotNull(getPath(field)));
    }

    @Override
    public void nullProcess(String field) {
        restrictions.add(criteriaBuilder.isNull(getPath(field)));
    }

    @Override
    public void parseFilter(Map<String, Object> filter) {
        filter.forEach((key, value) -> {
            if (value instanceof List) {
                Operator.List.getParseInstanceAndParse(key,
                        value,
                        this);
            } else if (value instanceof Map) {
                Operator.Map.getParseInstanceAndParse(key,
                        value,
                        this);
            } else {
                Operator.Base.getParseInstanceAndParse(key,
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

    public Predicate[] getRestrictionsArray() {
        return restrictions.toArray(new Predicate[0]);
    }

    public void parseFilters(List<?> values, Consumer<JpaStrategy> andOr) {
        JpaStrategy andStrategy = new JpaStrategy(root,
                criteriaBuilder);
        for (Object value : values) {
            if (value instanceof Map) {
                JpaStrategy copyStrategy = new JpaStrategy(root,
                        criteriaBuilder);
                copyStrategy.parseFilter((Map<String, Object>) value);
                andStrategy.addRestriction((root1, criteriaBuilder1) -> criteriaBuilder1.and(copyStrategy.getRestrictionsArray()));
            }
        }
        andOr.accept(andStrategy);
    }

    @Override
    public void and(List<Object> values) {
        parseFilters(values,
                jpaStrategy -> {
                    restrictions.add(criteriaBuilder.and(jpaStrategy.getRestrictionsArray()));
                });
    }

    @Override
    public void or(List<Object> values) {
        parseFilters(values,
                jpaStrategy -> {
                    restrictions.add(criteriaBuilder.or(jpaStrategy.getRestrictionsArray()));
                });
    }

    @Override
    public void and(Map<String, Object> value) {
        JpaStrategy copyStrategy = new JpaStrategy(root,
                criteriaBuilder);
        copyStrategy.parseFilter(value);
        restrictions.add(criteriaBuilder.and(copyStrategy.getRestrictionsArray()));
    }

    @Override
    public void or(Map<String, Object> value) {
        JpaStrategy copyStrategy = new JpaStrategy(root,
                criteriaBuilder);
        copyStrategy.parseFilter(value);
        restrictions.add(criteriaBuilder.or(copyStrategy.getRestrictionsArray()));
    }

    @Override
    public void in(String field, List<Object> values) {
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(getPath(field));
        for (Object cv : values) {
            cv = ifEnumThenTransformValue(field,
                    cv);
            in.value(cv);
        }
        restrictions.add(in);
    }

    @Override
    public void childEntityProcess(String field, Map<String, Object> value) {
        JpaStrategy childStrategy = new JpaStrategy(getPath(field),
                criteriaBuilder);
        childStrategy.parseFilter(value);
        restrictions.addAll(childStrategy.getRestrictions());
    }
}
