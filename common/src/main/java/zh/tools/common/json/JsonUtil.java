package zh.tools.common.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonUtil {
    public static <T> JSONObject toJsonObject(T summary) {
        return JSONObject.parseObject(JSONObject.toJSONString(summary));
    }

    public static <T> JSONArray toJsonArray(T data) {
        return JSONArray.parseArray(JSON.toJSONString(data));
    }

}
