package zh.tools.jpa;

import cn.hutool.core.exceptions.ValidateException;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import zh.tools.common.map.FilterMap;

import java.io.Serializable;
import java.util.List;

public interface BaseFilterController<T, ID extends Serializable> extends BaseClient<T, ID> {
    BaseFilterService<T, ID> getService();

    @Override
    @PostMapping
    default T save(@RequestBody T t) {
        return getService()
                .save(t);
    }

    @Override
    @PostMapping("/bulk")
    default Iterable<T> save(@RequestBody List<T> list) {
        return getService()
                .save(list);
    }

    @Override
    @DeleteMapping
    default void remove(@RequestParam List<ID> ids) {
        getService()
                .removeByIds(ids);
    }

    @Override
    @GetMapping("/{id}")
    default T findById(@PathVariable ID id) {
        return getService()
                .findById(id)
                .orElseThrow(() -> new ValidateException("Id not found"));
    }

    @Override
    @PostMapping("/filter/all")
    default List<T> filterAll(@RequestBody FilterMap<String, Object> filterMap) {
        return getService()
                .filter(filterMap);
    }

    @Override
    @PostMapping("/filter")
    default Page<T> filter(@RequestBody JpaFilterRequest filterRequest) {
        return getService()
                .filter(filterRequest);
    }

    @Override
    @PostMapping("/count")
    default Long count(@RequestBody FilterMap<String, Object> filterMap) {
        return getService().count(filterMap);
    }
}
