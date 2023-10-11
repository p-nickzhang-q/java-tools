package zh.tools.common.bitmap;

import cn.hutool.core.util.ReflectUtil;

/**
 * 单个int存储,最多存32个布尔值,因为单个int转换成二进制可以有32位
 */
public class OneIntMap extends IntMap {
    private static final long serialVersionUID = 9043301928091133706L;


    public OneIntMap(Object t, String field) {
        super(1, t, field);
        super.ints[0] = (int) ReflectUtil.getFieldValue(t, field);
    }

    @Override
    protected void setBindInt() {
        ReflectUtil.setFieldValue(t, field, super.ints[0]);
    }


}
