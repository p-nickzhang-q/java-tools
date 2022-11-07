package zh.tools.common.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

    public static BigDecimal divide(BigDecimal v1, BigDecimal v2, int scale) {
        if (v2 == null || v2.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return v1.divide(v2,
                scale,
                RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(long v1, long v2, int scale) {
        return divide(BigDecimal.valueOf(v1),
                BigDecimal.valueOf(v2),
                scale);
    }

    public static BigDecimal percentage(long v1, long v2) {
        BigDecimal bigDecimal1 = BigDecimal.valueOf(v1);
        BigDecimal bigDecimal2 = BigDecimal.valueOf(v2);
        return percentage(bigDecimal1,
                bigDecimal2);
    }

    public static BigDecimal percentage(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        BigDecimal divide = divide(bigDecimal1.multiply(BigDecimal.valueOf(100)),
                bigDecimal2,
                1);
        BigDecimal full = BigDecimal.valueOf(100);
        if (divide.compareTo(full) > 0) {
            return full;
        }
        return divide;
    }

}
