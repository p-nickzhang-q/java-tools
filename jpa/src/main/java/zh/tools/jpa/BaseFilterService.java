package zh.tools.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import zh.tools.common.map.FilterMap;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.*;

public abstract class BaseFilterService<T, ID extends Serializable> {

    public abstract BaseRepositorySupport<T, ID> repository();

    protected Specification<T> parseFilter2Spec(Map<String, Object> filter, SearchHook<T> searchHook) {
        if (searchHook == null) {
            searchHook = new SearchHook<T>() {
            };
        }
        SearchHook<T> finalSearchHook = searchHook;
        return (root, criteriaQuery, cb) -> {
            List<Predicate> restrictions = new ArrayList<>();
            finalSearchHook.beforeParseFilter(restrictions,
                    root,
                    criteriaQuery,
                    cb,
                    new FilterMap<>(filter));
            SpecFilterUtil.parseFilter(root,
                    cb,
                    filter,
                    restrictions);
            return cb.and(restrictions.toArray(new Predicate[0]));
        };
    }

    public Page<T> filter(Map<String, Object> filter, Pageable pageable) {
        Specification<T> spec = parseFilter2Spec(filter,
                null);
        return repository()
                .findAll(spec,
                        pageable);
    }

    public Page<T> filter(FilterRequest filterRequest) {
        Specification<T> spec = parseFilter2Spec(filterRequest.getFilter(),
                null);
        return repository()
                .findAll(spec,
                        filterRequest.toPage());
    }

    public List<T> filter(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter,
                null);
        return repository()
                .findAll(spec);
    }

    public List<T> filter() {
        Specification<T> spec = parseFilter2Spec(new FilterMap<>(),
                null);
        return repository()
                .findAll(spec);
    }

    public Optional<T> filterOne(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter,
                null);
        return repository()
                .findOne(spec);
    }

    public List<T> filter(Map<String, Object> filter, Sort sort) {
        Specification<T> spec = parseFilter2Spec(filter,
                null);
        return repository()
                .findAll(spec,
                        sort);
    }

    public Long count(Map<String, Object> filter) {
        Specification<T> spec = parseFilter2Spec(filter,
                null);
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

    public Page<T> filter(FilterRequest filterRequest, SearchHook<T> searchHook) {
        Specification<T> spec = parseFilter2Spec(filterRequest.getFilter(),
                searchHook);
        Page<T> page = repository()
                .findAll(spec,
                        filterRequest.toPage());
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
