package zh.tools.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zh.tools.common.map.FilterMap;
import zh.tools.jpa.filterparse.FilterParser;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseFilterService<T, ID extends Serializable> {

    public abstract BaseRepositorySupport<T, ID> repository();

    protected Specification<T> parseFilter2Spec(Map<String, Object> filter, SearchHook<T> searchHook) {
        return (root, criteriaQuery, cb) -> {
            FilterParser filterParser = new FilterParser(root,
                    cb);
            List<Predicate> restrictions = filterParser.getRestrictions();
            searchHook.beforeParseFilter(restrictions,
                    root,
                    criteriaQuery,
                    cb,
                    new FilterMap<>(filter));
            filterParser.parseFilter(filter);
            return cb.and(filterParser.getRestrictionsArray());
        };
    }

    protected Specification<T> parseFilter2Spec(Map<String, Object> filter) {
        SearchHook<T> searchHook = new SearchHook<T>() {
        };
        return parseFilter2Spec(filter,
                searchHook);
    }

    public Page<T> filter(Map<String, Object> filter, Pageable pageable) {
        Specification<T> spec = parseFilter2Spec(filter);
        return repository()
                .findAll(spec,
                        pageable);
    }

    public Page<T> filter(JpaFilterRequest jpaFilterRequest) {
        Specification<T> spec = parseFilter2Spec(jpaFilterRequest.getFilter());
        return repository()
                .findAll(spec,
                        jpaFilterRequest.toPage());
    }

    public List<T> filter(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter);
        return repository()
                .findAll(spec);
    }

    public List<T> filter() {
        Specification<T> spec = parseFilter2Spec(new FilterMap<>());
        return repository()
                .findAll(spec);
    }

    public Optional<T> filterOne(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter);
        return repository()
                .findOne(spec);
    }

    public List<T> filter(Map<String, Object> filter, Sort sort) {
        Specification<T> spec = parseFilter2Spec(filter);
        return repository()
                .findAll(spec,
                        sort);
    }

    public Long count(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter);
        return repository()
                .count(spec);
    }

    public Page<T> filter(Map<String, Object> filter, Pageable pageable, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filter,
                searchHook);
        Page<T> page = repository()
                .findAll(spec,
                        pageable);
        searchHook.beforeReturn(page.getContent());
        return page;
    }

    public Page<T> filter(JpaFilterRequest jpaFilterRequest, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(jpaFilterRequest.getFilter(),
                searchHook);
        Page<T> page = repository()
                .findAll(spec,
                        jpaFilterRequest.toPage());
        searchHook.beforeReturn(page.getContent());
        return page;
    }

    public List<T> filter(Map<String, Object> filter, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filter,
                searchHook);
        List<T> list = repository()
                .findAll(spec);
        searchHook.beforeReturn(list);
        return list;
    }

    public List<T> filter(SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(new FilterMap<>(),
                searchHook);
        List<T> list = repository()
                .findAll(spec);
        searchHook.beforeReturn(list);
        return list;
    }

    public Optional<T> filterOne(Map<String, Object> filter, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filter,
                searchHook);
        Optional<T> one = repository()
                .findOne(spec);
        one.ifPresent(t -> {
            searchHook.beforeReturn(Collections.singletonList(t));
        });
        return one;
    }

    public List<T> filter(Map<String, Object> filter, Sort sort, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filter,
                searchHook);
        List<T> list = repository()
                .findAll(spec,
                        sort);
        searchHook.beforeReturn(list);
        return list;
    }

    public Long count(Map<String, Object> filter, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filter,
                searchHook);
        return repository()
                .count(spec);
    }

}
