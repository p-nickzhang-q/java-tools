package zh.tools.jpa;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import zh.tools.common.map.FilterMap;

import java.io.Serializable;
import java.util.List;

public interface BaseClient<T, ID extends Serializable> {
    @PostMapping
    T save(@RequestBody T t);

    @PostMapping("/bulk")
    Iterable<T> save(@RequestBody List<T> list);

    @DeleteMapping
    void remove(@RequestParam List<ID> ids);

    @GetMapping("/{id}")
    T findById(@PathVariable ID id);

    @PostMapping("/filter/all")
    List<T> filterAll(@RequestBody FilterMap<String, Object> filterMap);

    @PostMapping("/filter")
    Page<T> filter(@RequestBody JpaFilterRequest filterRequest);

    @PostMapping("/count")
    Long count(@RequestBody FilterMap<String, Object> filterMap);
}
