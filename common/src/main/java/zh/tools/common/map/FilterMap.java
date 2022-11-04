package zh.tools.common.map;

import cn.hutool.core.exceptions.ValidateException;
import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NoArgsConstructor
public class FilterMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 4841473996984032777L;

    public FilterMap(Map<K, V> filter) {
        this.putAll(filter);
    }

    public Map<String, Object> getMap(String k) {
        V v = this.get(k);
        if (v == null) {
            return null;
        }
        return (Map<String, Object>) v;
    }

    public String getString(String k) {
        V v = this.get(k);
        if (v == null) {
            return null;
        }
        return (String) v;
    }

    public Long getLong(String k) {
        V v = this.get(k);
        if (v == null) {
            return null;
        }
        return (Long) v;
    }

    public V getRequiredValue(K k) {
        V v = this.get(k);
        if (v == null) {
            throw new ValidateException("%s为null");
        }
        return v;
    }

    public JSONObject toJson() {
        return (JSONObject) JSONObject.toJSON(this);
    }

    public Number getNumber(String k) {
        V v = this.get(k);
        if (v == null) {
            return null;
        }
        return (Number) v;
    }

    public FilterMap<K, V> set(K k, V val) {
        this.put(k,
                val);
        return this;
    }

    public FilterMap<K, V> wrapper(K k, Consumer<FilterMap<String, Object>> wrapper) {
        FilterMap<String, Object> filterMap = new FilterMap<>();
        this.put(k,
                (V) filterMap);
        wrapper.accept(filterMap);
        return this;
    }

    public FilterMap<K, V> wrapList(K k, Consumer<List<FilterMap<String, Object>>> wrapList) {
        List<FilterMap<String, Object>> list = new ArrayList<>();
        this.put(k,
                (V) list);
        wrapList.accept(list);
        return this;
    }

    public static FilterMap<String, Object> newFilterMap() {
        return new FilterMap<>();
    }

    public static void main(String[] args) {
        FilterMap<String, Object> filterMap = FilterMap
                .newFilterMap()
                .wrapper("user",
                        user -> user
                                .set("id",
                                        123)
                                .wrapper("createDate",
                                        createDate -> createDate
                                                .set("$start",
                                                        0)
                                                .set("$end",
                                                        1)))
                .set("questionListName",
                        "test");
        System.out.println(filterMap);
    }
}
