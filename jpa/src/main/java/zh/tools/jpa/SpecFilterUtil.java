package zh.tools.jpa;

import cn.hutool.core.util.StrUtil;
import zh.tools.common.consts.SpecOperator;
import zh.tools.common.time.JavaTimeUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class SpecFilterUtil {

    public final static String DATE_REG = "\\d{4}-\\d{2}-\\d{2}";

    public static <T> void parseFilter(Path<T> root, CriteriaBuilder cb, Map<String, Object> filter, List<Predicate> predicates) {
        filter.forEach((k, v) -> {
            if (v instanceof List) {
                listProcess(root,
                        cb,
                        predicates,
                        k,
                        (List<Object>) v);
            } else if (v instanceof Map) {
                mapProcess(root,
                        cb,
                        predicates,
                        k,
                        v);
            } else {
                /*基础属性处理*/
                /*predicate == null时，不加入predicates*/
                getBaseDataPredicate(cb,
                        root.get(k),
                        v)
                        .ifPresent(predicates::add);
            }
        });
    }

    private static Enum processEnumValueFilter(Path<Object> path, String v) {
        Class javaType = path.getJavaType();
        return Enum.valueOf(javaType,
                v);
    }

    private static Optional<Predicate> getBaseDataPredicate(CriteriaBuilder cb, Path<Object> path, Object v) {
        Predicate predicate = null;
        if (v instanceof String) {
            if (StrUtil.isNotBlank(v.toString())) {
                if (SpecOperator.N.equals(v)) {
                    predicate = cb.isNull(path);
                } else if (SpecOperator.NN.equals(v)) {
                    predicate = cb.isNotNull(path);
                } else if (path
                        .getJavaType()
                        .isEnum()) {
                    v = processEnumValueFilter(path,
                            v.toString());
                    predicate = cb.equal(path,
                            v);
                } else {
                    /*字符串模糊查询*/
                    if (v
                            .toString()
                            .contains("%")) {
                        predicate = cb.like(path.as(String.class),
                                v.toString());
                    } else {
                        /*字符串精确查询*/
                        predicate = cb.equal(path.as(String.class),
                                v.toString());
                    }
                }
            }
        } else if (v != null) {
            predicate = cb.equal(path,
                    v);
        }
        return Optional.ofNullable(predicate);
    }

    private static final BiFunction<CriteriaBuilder, List<Predicate>, Predicate> or = (cb, orAndPredicates) -> cb.or(orAndPredicates.toArray(new Predicate[0]));
    private static final BiFunction<CriteriaBuilder, List<Predicate>, Predicate> and = (cb, orAndPredicates) -> cb.and(orAndPredicates.toArray(new Predicate[0]));

    private static <T> void mapProcess(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String k, Object v) {
        if (k.equals(SpecOperator.OR) || k.equals(SpecOperator.AND)) {
            List<Predicate> orAndPredicates = new ArrayList<>();
            parseFilter(root,
                    cb,
                    (Map<String, Object>) v,
                    orAndPredicates);
            Predicate predicate;
            if (k.equals(SpecOperator.OR)) {
                predicate = or.apply(cb,
                        orAndPredicates);
            } else {
                predicate = and.apply(cb,
                        orAndPredicates);
            }
            predicates.add(predicate);
        } else {
            ((Map) v).forEach((ck, cv) -> {
                switch (ck.toString()) {
                    case SpecOperator.START:
                    case SpecOperator.GE:
                        parseGe(root,
                                cb,
                                predicates,
                                k,
                                cv);
                        break;
                    case SpecOperator.GT:
                        parseGt(root,
                                cb,
                                predicates,
                                k,
                                cv);
                        break;
                    case SpecOperator.LT:
                        parseLt(root,
                                cb,
                                predicates,
                                k,
                                cv);
                        break;
                    case SpecOperator.END:
                    case SpecOperator.LE:
                        parseLe(root,
                                cb,
                                predicates,
                                k,
                                cv);
                        break;
                    case SpecOperator.BETWEEN:
                        processBetween(root,
                                cb,
                                predicates,
                                k,
                                cv);
                        break;
                    case SpecOperator.NE:
                        cv = getCv(root,
                                k,
                                cv);
                        predicates.add(cb.notEqual(root.get(k),
                                cv));
                        break;
                    case SpecOperator.EQ:
                        predicates.add(cb.equal(root.get(k),
                                cv));
                        break;
                    default:
                        /*子对象属性查询*/
                        parseFilter(root.get(k),
                                cb,
                                (Map<String, Object>) v,
                                predicates);
                        break;
                }
            });
        }
    }

    private static <T> void processBetween(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String field, Object cv) {
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
            predicate = cb.between(root.get(field),
                    (Comparable) temp[0],
                    (Comparable) temp[1]);
            predicates.add(predicate);
        }
    }

    private static <T> void parseLe(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String field, Object cv) {
        processCompare(root,
                predicates,
                field,
                cv,
                new SpecCompare(cb::lessThanOrEqualTo,
                        cb::lessThanOrEqualTo,
                        cb::lessThanOrEqualTo,
                        cb::le,
                        cb::lessThanOrEqualTo));
    }

    private static <T> void processCompare(Path<T> root, List<Predicate> predicates, String field, Object cv, SpecCompare compare) {
        Predicate predicate;
        if (cv instanceof String) {
            if (JavaTimeUtil.matchDateReg(cv.toString())) {
                predicate = compare.localDateCompare.compare(root.get(field),
                        JavaTimeUtil.str2LocalDate(cv.toString()));
            } else if (JavaTimeUtil.matchDateTimeReg(cv.toString())) {
                predicate = compare.localDateTimeCompare.compare(root.get(field),
                        JavaTimeUtil.str2LocalDateTime(cv.toString()));
            } else {
                predicate = compare.pathCompare.compare(root.get(field),
                        root.get(cv.toString()));
            }
        } else if (cv instanceof Date) {
            predicate = compare.dateCompare.compare(root.get(field),
                    ((Date) cv));
        } else {
            predicate = compare.numCompare.compare(root.get(field),
                    ((Number) cv));
        }
        predicates.add(predicate);
    }

    private static <T> void parseLt(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String field, Object cv) {
        processCompare(root,
                predicates,
                field,
                cv,
                new SpecCompare(cb::lessThan,
                        cb::lessThan,
                        cb::lessThan,
                        cb::lt,
                        cb::lessThan));
    }

    private static <T> void parseGt(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String field, Object cv) {
        processCompare(root,
                predicates,
                field,
                cv,
                new SpecCompare(cb::greaterThan,
                        cb::greaterThan,
                        cb::greaterThan,
                        cb::gt,
                        cb::greaterThan));
    }

    private static <T> void parseGe(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String field, Object cv) {
        processCompare(root,
                predicates,
                field,
                cv,
                new SpecCompare(cb::greaterThanOrEqualTo,
                        cb::greaterThanOrEqualTo,
                        cb::greaterThanOrEqualTo,
                        cb::ge,
                        cb::greaterThanOrEqualTo));
    }

    private static <T> void listProcess(Path<T> root, CriteriaBuilder cb, List<Predicate> predicates, String k, List<Object> v) {
        switch (k) {
            /*or*/
            case SpecOperator.OR:
                List<Predicate> orPredicates = getPredicates(root,
                        cb,
                        v);
                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
                break;
            /*and*/
            case SpecOperator.AND:
                List<Predicate> andAllPredicates = getPredicates(root,
                        cb,
                        v);
                predicates.add(cb.and(andAllPredicates.toArray(new Predicate[0])));
                break;
            /*in*/
            default:
                CriteriaBuilder.In<Object> in = cb.in(root.get(k));
                v.forEach(cv -> {
                    cv = getCv(root,
                            k,
                            cv);
                    in.value(cv);
                });
                predicates.add(in);
                break;
        }
    }

    private static <T> Object getCv(Path<T> root, String k, Object cv) {
        if (root
                .get(k)
                .getJavaType()
                .isEnum()) {
            cv = processEnumValueFilter(root.get(k),
                    cv.toString());
        }
        return cv;
    }

    private static <T> List<Predicate> getPredicates(Path<T> root, CriteriaBuilder cb, List<?> v) {
        List<Predicate> predicates = new ArrayList<>();
        v.forEach(cvItem -> {
            List<Predicate> andPredicates = new ArrayList<>();
            if (cvItem instanceof Map) {
                parseFilter(root,
                        cb,
                        (Map<String, Object>) cvItem,
                        andPredicates);
            }
            predicates.add(cb.and(andPredicates.toArray(new Predicate[0])));
        });
        return predicates;
    }

}