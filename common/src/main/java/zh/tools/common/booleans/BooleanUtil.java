package zh.tools.common.booleans;

import java.math.BigDecimal;

public class BooleanUtil {
    public static boolean of(Integer val) {
        return val != null && val != 0;
    }

    public static boolean of(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) != 0;
    }
}
