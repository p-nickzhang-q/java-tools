package zh.tools.jpa;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import zh.tools.common.consts.SpecOperator;
import zh.tools.common.time.JavaTimeUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@Data
public class FilterParser {

    public final static String DATE_REG = "\\d{4}-\\d{2}-\\d{2}";
    private final Path<?> root;
    private final CriteriaBuilder criteriaBuilder;
    private final List<Predicate> restrictions = new ArrayList<>();

    public FilterParser(Path<?> root, CriteriaBuilder criteriaBuilder) {
        this.root = root;
        this.criteriaBuilder = criteriaBuilder;
    }

    public void parseFilter(Map<String, Object> filter) {
        filter.forEach((k, v) -> {
            if (v instanceof List) {
                listParse(k,
                        (List<Object>) v);
            } else if (v instanceof Map) {
                mapParse(k,
                        (Map<String, Object>) v);
            } else {
                /*基础属性处理*/
                /*predicate == null时，不加入predicates*/
                baseDataParse(k,
                        v);
            }
        });
    }

    private void baseDataParse(String k, Object v) {
        Path<Object> path = root.get(k);
        Predicate predicate = null;
        if (v instanceof String) {
            if (StrUtil.isNotBlank(v.toString())) {
                if (SpecOperator.N.equals(v)) {
                    predicate = criteriaBuilder.isNull(path);
                } else if (SpecOperator.NN.equals(v)) {
                    predicate = criteriaBuilder.isNotNull(path);
                } else if (path
                        .getJavaType()
                        .isEnum()) {
                    v = ifEnumThenTransformValue(k,
                            v);
                    predicate = criteriaBuilder.equal(path,
                            v);
                } else {
                    /*字符串模糊查询*/
                    if (v
                            .toString()
                            .contains("%")) {
                        predicate = criteriaBuilder.like(path.as(String.class),
                                v.toString());
                    } else {
                        /*字符串精确查询*/
                        predicate = criteriaBuilder.equal(path.as(String.class),
                                v.toString());
                    }
                }
            }
        } else if (v != null) {
            predicate = criteriaBuilder.equal(path,
                    v);
        }
        if (predicate != null) {
            criteriaBuilder.and(predicate);
        }
    }

    private void mapParse(String k, Map<String, Object> value) {
        boolean ifAndOr = k.equals(SpecOperator.OR) || k.equals(SpecOperator.AND);
        FilterParser cloneRootAndCriteriaBuilderFilterParser = cloneRootAndCriteriaBuilderFilterParser();
        if (ifAndOr) {
            cloneRootAndCriteriaBuilderFilterParser.parseFilter(value);
            Predicate[] restrictionsArray = cloneRootAndCriteriaBuilderFilterParser.getRestrictionsArray();
            if (k.equals(SpecOperator.OR)) {
                restrictions.add(criteriaBuilder.or(restrictionsArray));
            } else {
                restrictions.add(criteriaBuilder.and(restrictionsArray));
            }
        } else {
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String ck = entry.getKey();
                Object cv = entry.getValue();
                switch (ck) {
                    case SpecOperator.START:
                    case SpecOperator.GE:
                        parseGe(k,
                                cv);
                        break;
                    case SpecOperator.GT:
                        parseGt(k,
                                cv);
                        break;
                    case SpecOperator.LT:
                        parseLt(k,
                                cv);
                        break;
                    case SpecOperator.END:
                    case SpecOperator.LE:
                        parseLe(k,
                                cv);
                        break;
                    case SpecOperator.BETWEEN:
                        processBetween(k,
                                cv);
                        break;
                    case SpecOperator.NE:
                        cv = ifEnumThenTransformValue(k,
                                cv);
                        restrictions.add(criteriaBuilder.notEqual(root.get(k),
                                cv));
                        break;
                    case SpecOperator.EQ:
                        restrictions.add(criteriaBuilder.equal(root.get(k),
                                cv));
                        break;
                    default:
                        /*子对象属性查询*/
                        cloneRootAndCriteriaBuilderFilterParser.parseFilter(value);
                        break;
                }
            }
        }
    }

    private void processBetween(String field, Object cv) {
        Object[] temp;
        if (cv instanceof List) {
            temp = ((List) cv).toArray();
        } else {
            temp = (Object[]) cv;
        }
        if (temp != null && temp.length > 0) {
            Predicate predicate;
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
            predicate = criteriaBuilder.between(root.get(field),
                    (Comparable) temp[0],
                    (Comparable) temp[1]);
            restrictions.add(predicate);
        }
    }

    private void parseLe(String field, Object cv) {
        processCompare(field,
                cv,
                new SpecCompare(criteriaBuilder::lessThanOrEqualTo));
    }


    public void processCompare(String field, Object value, SpecCompare compare) {
        Predicate predicate;
        if (value instanceof String) {
            if (JavaTimeUtil.matchDateReg(value.toString())) {
                value = JavaTimeUtil.str2LocalDate(value.toString());
            } else if (JavaTimeUtil.matchDateTimeReg(value.toString())) {
                value = JavaTimeUtil.str2LocalDateTime(value.toString());
            } else {
                value = root.get(value.toString());
            }
        }
        predicate = compare.compare.compare(root.get(field),
                ((Comparable) value));
        restrictions.add(predicate);
    }

    private void parseLt(String field, Object cv) {
        processCompare(field,
                cv,
                new SpecCompare(criteriaBuilder::lessThan));
    }

    private void parseGt(String field, Object cv) {
        processCompare(field,
                cv,
                new SpecCompare(criteriaBuilder::greaterThan));
    }

    private void parseGe(String field, Object cv) {
        processCompare(field,
                cv,
                new SpecCompare(criteriaBuilder::greaterThanOrEqualTo));
    }

    private void listParse(String k, List<Object> v) {
        switch (k) {
            /*or*/
            case SpecOperator.OR:
                parseFilters(v,
                        cloneFilterParser -> criteriaBuilder.or(cloneFilterParser.getRestrictionsArray()));
                break;
            /*and*/
            case SpecOperator.AND:
                parseFilters(v,
                        cloneFilterParser -> criteriaBuilder.and(cloneFilterParser.getRestrictionsArray()));
                break;
            /*in*/
            default:
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get(k));
                for (Object cv : v) {
                    cv = ifEnumThenTransformValue(k,
                            cv);
                    in.value(cv);
                }
                restrictions.add(in);
                break;
        }
    }

    private Object ifEnumThenTransformValue(String k, Object cv) {
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

    private void parseFilters(List<?> values, Function<FilterParser, Predicate> andOr) {
        for (Object value : values) {
            if (value instanceof Map) {
                FilterParser cloneRootAndCriteriaBuilderFilterParser = cloneRootAndCriteriaBuilderFilterParser();
                cloneRootAndCriteriaBuilderFilterParser.parseFilter(((Map<String, Object>) value));
                andOr.apply(cloneRootAndCriteriaBuilderFilterParser);
            }
        }
    }

    public FilterParser cloneRootAndCriteriaBuilderFilterParser() {
        return new FilterParser(root,
                criteriaBuilder);
    }

    public Predicate[] getRestrictionsArray() {
        return restrictions.toArray(new Predicate[0]);
    }

}
