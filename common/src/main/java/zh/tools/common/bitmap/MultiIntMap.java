package zh.tools.common.bitmap;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 多个int存储,可以存很多,用字符串绑定,多个int以逗号分隔
 */
public class MultiIntMap extends IntMap {
    public MultiIntMap(int size, Object t, String field) {
        super(size, t, field);
        setIntsString((String) ReflectUtil.getFieldValue(t, field));
    }

    public String getIntsString() {
        return Arrays.stream(super.ints).mapToObj(i -> Integer.valueOf(i).toString()).collect(Collectors.joining(","));
    }

    public void setIntsString(String ints) {
        if (StrUtil.isBlank(ints)) {
            ints = "0";
        }
        String[] split = ints.split(",");
        int[] intArray = new int[split.length];
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            intArray[i] = Integer.parseInt(split[i]);
        }
        super.ints = intArray;
    }

    @Override
    protected void setBindInt() {
        ReflectUtil.setFieldValue(t, field, getIntsString());
    }

}
