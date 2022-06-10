package zh.tools.specfilter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import zh.tools.map.FilterMap;

import java.io.Serializable;

/**
 * @author nickzhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterRequest implements Serializable {
    private static final long serialVersionUID = 8067018567014818527L;
    private FilterMap<String, Object> filter = new FilterMap<>();
    private Sort.Direction direction = Sort.Direction.ASC;
    private String sortProperty = "id";
    private Integer page = 0;
    private Integer size = 10;

    public FilterRequest put(String key, Object value) {
        filter.put(key, value);
        return this;
    }

    public Pageable toPage() {
        return PageRequest.of(page, size, Sort.by(direction, sortProperty.split(",")));
    }
}
