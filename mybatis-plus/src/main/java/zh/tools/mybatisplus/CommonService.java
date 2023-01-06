package zh.tools.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zh.tools.common.map.FilterMap;
import zh.tools.common.paging.FilterRequest;
import zh.tools.mybatisplus.filterparse.FilterParser;

import java.io.Serializable;
import java.util.List;

public class CommonService<Mapper extends BaseMapper<Entity>, Entity extends BaseEntity<ID>, ID extends Serializable> extends ServiceImpl<Mapper, Entity> {

    public IPage<Entity> filter(FilterRequest filterRequest) {
        return null;
    }

    public List<Entity> filter(FilterMap<String, Object> filter, FilterRequest.Order... orders) {
        QueryWrapper<Entity> queryWrapper = parseFilterAndOrdersToQueryWrapper(filter,
                orders);
        return null;
    }

    private QueryWrapper<Entity> parseFilterAndOrdersToQueryWrapper(FilterMap<String, Object> filter, FilterRequest.Order[] orders) {
        FilterParser<Entity> filterParser = new FilterParser<>();
        QueryWrapper<Entity> queryWrapper = filterParser.parseFilter(filter);
        return null;
    }

    public List<Entity> filter(FilterMap<String, Object> filter) {
        return filter(filter,
                new FilterRequest.Order());
    }
}
