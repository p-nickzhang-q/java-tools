package zh.tools.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zh.tools.common.map.FilterMap;
import zh.tools.common.paging.FilterRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CommonService<Mapper extends BaseMapper<Entity>, Entity extends BaseEntity<ID>, ID extends Serializable> extends ServiceImpl<Mapper, Entity> {

    public IPage<Entity> filter(FilterRequest filterRequest) {
        /*mybatisplus 从第一页开始*/
        int pageNo = filterRequest.getPage() + 1;
        Page<Entity> page = new Page<>(pageNo,
                filterRequest.getSize());
        QueryWrapper<Entity> queryWrapper = parseFilterAndOrdersToQueryWrapper(filterRequest.getFilter(),
                filterRequest
                        .getOrders()
                        .toArray(new FilterRequest.Order[0]));
        return getBaseMapper()
                .selectPage(page,
                        queryWrapper);
    }

    public List<Entity> filter(FilterMap<String, Object> filter, FilterRequest.Order... orders) {
        QueryWrapper<Entity> queryWrapper = parseFilterAndOrdersToQueryWrapper(filter,
                orders);
        return getBaseMapper()
                .selectList(queryWrapper);
    }

    private QueryWrapper<Entity> parseFilterAndOrdersToQueryWrapper(FilterMap<String, Object> filter, FilterRequest.Order[] orders) {
        FilterParser<Entity> filterParser = new FilterParser<>();
        QueryWrapper<Entity> queryWrapper = filterParser.parseFilter(filter);
        addOrders(orders,
                queryWrapper);
        return queryWrapper;
    }

    private void addOrders(FilterRequest.Order[] orders, QueryWrapper<Entity> queryWrapper) {
        for (FilterRequest.Order order : orders) {
            boolean isAsc = Objects.equals(order.getDirection(),
                    FilterRequest.Direction.ASC);
            if (isAsc) {
                queryWrapper.orderByAsc(MyBatisPlusStrategy.humpToUnderline(order.getSortProperty()));
            } else {
                queryWrapper.orderByDesc(MyBatisPlusStrategy.humpToUnderline(order.getSortProperty()));
            }
        }
    }

    public List<Entity> filter(FilterMap<String, Object> filter) {
        return filter(filter,
                new FilterRequest.Order());
    }

    public List<Entity> filter() {
        return filter(FilterMap.newFilterMap(),
                new FilterRequest.Order());
    }
}
