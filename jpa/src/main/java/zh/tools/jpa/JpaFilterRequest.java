package zh.tools.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import zh.tools.common.paging.FilterRequest;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * @author nickzhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class JpaFilterRequest extends FilterRequest implements Serializable {

    public Pageable toPage() {
        return PageRequest.of(page,
                size,
                Sort.by(orders
                        .stream()
                        .map(order -> new Sort.Order(Sort.Direction.valueOf(order
                                .getDirection()
                                .name()),
                                order.getSortProperty()))
                        .collect(Collectors.toList())));
    }
}
