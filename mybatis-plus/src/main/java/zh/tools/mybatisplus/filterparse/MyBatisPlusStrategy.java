package zh.tools.mybatisplus.filterparse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import zh.tools.common.filterparse.ParseStrategy;
import zh.tools.mybatisplus.enums.Operator;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class MyBatisPlusStrategy implements ParseStrategy {
    private final QueryWrapper<?> queryWrapper;

    public MyBatisPlusStrategy(QueryWrapper<?> queryWrapper) {
        this.queryWrapper = queryWrapper;
    }

    public static String humpToUnderline(String key) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern
                .compile(regex)
                .matcher(key);
        while (matcher.find()) {
            String target = matcher.group();
            key = key.replaceAll(target,
                    "_" + target.toLowerCase());
        }
        return key;
    }

    @Override
    public void fuzzy(String field, Object value) {
        String likeString = value
                .toString()
                .replaceAll("%",
                        "");
        /*like会自动添加%,所以要去%*/
        queryWrapper.like(field,
                likeString);
    }

    @Override
    public void between(String field, Object value) {
        Object[] temp;
        if (value instanceof List) {
            temp = ((List) value).toArray();
        } else {
            temp = (Object[]) value;
        }
        queryWrapper.between(field,
                temp[0],
                temp[1]);
    }

    @Override
    public void equal(String field, Object value) {
        queryWrapper.eq(field,
                value);
    }

    @Override
    public void greaterEqual(String field, Object value) {
        queryWrapper.ge(field,
                value);
    }

    @Override
    public void greaterThan(String field, Object value) {
        queryWrapper.gt(field,
                value);
    }

    @Override
    public void lessThan(String field, Object value) {
        queryWrapper.lt(field,
                value);
    }

    @Override
    public void lessEqual(String field, Object value) {
        queryWrapper.le(field,
                value);
    }

    @Override
    public void start(String field, String value) {
        queryWrapper.ge(field,
                String.format("%s 00:00:00",
                        value));
    }

    @Override
    public void end(String field, String value) {
        queryWrapper.le(field,
                String.format("%s 23:59:59",
                        value));
    }

    @Override
    public void notEqual(String field, Object value) {
        queryWrapper.ne(field,
                value);
    }

    @Override
    public void notNull(String field) {
        queryWrapper.isNotNull(field);
    }

    @Override
    public void nullProcess(String field) {
        queryWrapper.isNull(field);
    }

    @Override
    public void parseFilter(Map<String, Object> filter) {
        filter.forEach((key, value) -> {
            String dataBaseField = MyBatisPlusStrategy.humpToUnderline(key);
            if (value instanceof List) {
                Operator.List.getParseInstanceAndParse(dataBaseField,
                        value,
                        this);
            } else if (value instanceof Map) {
                Operator.Map.getParseInstanceAndParse(dataBaseField,
                        value,
                        this);
            } else {
                Operator.Base.getParseInstanceAndParse(dataBaseField,
                        value,
                        this);
            }
        });
    }

    @Override
    public void and(List<Object> values) {
        queryWrapper.and(queryWrapper1 -> {
            MyBatisPlusStrategy childStrategy = new MyBatisPlusStrategy(queryWrapper1);
            for (Object value : values) {
                if (value instanceof Map) {
                    childStrategy.parseFilter((Map<String, Object>) value);
                }
            }
        });
    }

    @Override
    public void or(List<Object> values) {
        queryWrapper.and(queryWrapper1 -> {
            MyBatisPlusStrategy childStrategy = new MyBatisPlusStrategy(queryWrapper1);
            for (Object value : values) {
                if (value instanceof Map) {
                    childStrategy.parseFilter((Map<String, Object>) value);
                }
                queryWrapper1.or();
            }
        });
    }

    @Override
    public void and(Map<String, Object> value) {
        queryWrapper.and(queryWrapper1 -> {
            MyBatisPlusStrategy childStrategy = new MyBatisPlusStrategy(queryWrapper1);
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String childDataBaseField = humpToUnderline(entry.getKey());
                Operator.Base.getParseInstanceAndParse(childDataBaseField,
                        entry.getValue(),
                        childStrategy);
            }
        });
    }

    @Override
    public void or(Map<String, Object> value) {
        queryWrapper.and(queryWrapper1 -> {
            MyBatisPlusStrategy childStrategy = new MyBatisPlusStrategy(queryWrapper1);
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String childDataBaseField = humpToUnderline(entry.getKey());
                Operator.Base.getParseInstanceAndParse(childDataBaseField,
                        entry.getValue(),
                        childStrategy);
                queryWrapper1.or();
            }
        });
    }

    @Override
    public void in(String field, List<Object> values) {
        queryWrapper.in(field,
                values);
    }
}