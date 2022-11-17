package zh.tools.common.paging;

import lombok.Data;
import lombok.experimental.Accessors;
import zh.tools.common.map.FilterMap;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
public class FilterRequest {
    private static final long serialVersionUID = 8067018567014818527L;
    protected List<Order> orders = Collections.singletonList(new Order());
    protected Integer page = 0;
    protected Integer size = 10;
    private FilterMap<String, Object> filter = new FilterMap<>();

    public FilterRequest put(String key, Object value) {
        filter.put(key,
                value);
        return this;
    }

    public enum Direction {
        DESC, ASC
    }

    @Accessors(chain = true)
    @Data
    public static class Order {
        private String sortProperty = "id";
        private Direction direction = Direction.ASC;
    }
}
