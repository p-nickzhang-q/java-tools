package zh.tools.time;

import lombok.Data;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Data
public class Quarter implements TimeUnit {
    private final int year;
    private final int q;

    public final Map<Integer, Integer[]> MONTH_MAP = new HashMap<Integer, Integer[]>() {{
        put(1,
                new Integer[]{1, 2, 3});
        put(2,
                new Integer[]{4, 5, 6});
        put(3,
                new Integer[]{7, 8, 9});
        put(4,
                new Integer[]{10, 11, 12});
    }};

    public Quarter(int year, int q) {
        this.year = year;
        this.q = q;
    }

    //yyyy-q解析季度格式
    public static Quarter parse(String s) {
        String[] split = s.split("-");
        int year = Year
                .parse(split[0])
                .getValue();
        int q = Integer.parseInt(split[1]);
        return new Quarter(year,
                q);
    }

    @Override
    public int getDays() {
        int days = 0;
        for (Integer month : MONTH_MAP.get(this.getQ())) {
            days += LocalDate
                    .now()
                    .withMonth(month)
                    .lengthOfMonth();
        }
        return days;
    }

    @Override
    public String toString() {
        return String.format("%d年第%d季度",
                year,
                q);
    }

    @Override
    public String uniqueString() {
        return String.format("%d-%d",
                year,
                q);
    }
}
