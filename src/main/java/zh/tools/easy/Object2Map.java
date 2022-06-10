package zh.tools.easy;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 将对象数据,转化为一层数据的map,子对象用分隔符标志,如child.name
 */
public class Object2Map {

    private final Map<String, Object> rootMap = new HashMap<>();

    private String split = ".";
    private final Object data;

    public Object2Map(String split, Object data) {
        this.split = split;
        this.data = data;
    }

    public Map<String, Object> run() {
        Map map = object2Map(this.data);
        transform(map, "");
        return rootMap;
    }

    private Map object2Map(Object data) {
        return JSONObject.parseObject(JSONObject.toJSONString(data), Map.class);
    }

    private void transform(Map<String, Object> map, String parent) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key;
            if (parent.isEmpty()) {
                key = entry.getKey();
            } else {
                key = String.format("%s%s%s", parent, this.split, entry.getKey());
            }
            if (entry.getValue() instanceof JSONObject) {
                Map temp = object2Map(entry.getValue());
                transform(temp, key);
            } else {
                rootMap.put(key, entry.getValue());
            }
        }
    }

}
