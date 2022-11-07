package zh.tools.common.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface Json {

    Object getData();

    default JSONObject toJsonObject() {
        return JsonUtil.toJsonObject(getData());
    }

    default JSONArray toJsonArray() {
        return JsonUtil.toJsonArray(getData());
    }

    default <T> T toObject(Class<T> tClass) {
        return JSONObject.parseObject(JSON.toJSONString(getData()),
                tClass);
    }

    default <T> List<T> toObjectList(Class<T> tClass) {
        return JSONArray.parseArray(JSON.toJSONString(getData()),
                tClass);
    }
}
