package zh.tools.jpa;


import zh.tools.common.map.FilterMap;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public interface SearchHook<T> {
    default void beforeParseFilter(List<Predicate> predicates, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, FilterMap<String, Object> filter) {

    }

    default void beforeReturn(List<T> entities) {
    }

}
