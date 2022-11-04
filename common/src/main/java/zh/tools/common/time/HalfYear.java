package zh.tools.common.time;

import lombok.Data;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Data
public class HalfYear implements TimeUnit {
    private final int year;
    private final boolean isFirstHalf;

    public final Map<Boolean, Integer[]> QUARTER_MAP = new HashMap<Boolean, Integer[]>() {{
        put(true,
                new Integer[]{1, 2});
        put(false,
                new Integer[]{3, 4});
    }};

    public HalfYear(int year, boolean isFirstHalf) {
        this.year = year;
        this.isFirstHalf = isFirstHalf;
    }

    //yyyy-{0,1}解析半年格式
    public static HalfYear parse(String s) {
        String[] split = s.split("-");
        int year = Year
                .parse(split[0])
                .getValue();
        boolean isFirstHalf = Integer.parseInt(split[1]) == 1;
        return new HalfYear(year,
                isFirstHalf);
    }

    @Override
    public int getDays() {
        int days = 0;
        for (Integer q : QUARTER_MAP.get(isFirstHalf)) {
            days += new Quarter(year,
                    q).getDays();
        }
        return days;
    }

    @Override
    public String toString() {
        return String.format("%d年%s半年",
                year,
                isFirstHalf ? "上" : "下");
    }

    @Override
    public String uniqueString() {
        return String.format("%d-%d",
                year,
                isFirstHalf ? 1 : 0);
    }

}
