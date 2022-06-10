package zh.tools.time;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

@Data
public class TimeProcess {

    private LocalDateTime time;

    private TimeProcess(LocalDateTime time) {
        this.time = time;
    }

    public static TimeProcess of(Long time) {
        LocalDateTime localDateTime = JavaTimeUtil.long2LocalDateTime(time);
        return new TimeProcess(localDateTime);
    }

    public static TimeProcess of(String time) {
        LocalDateTime localDateTime = JavaTimeUtil.str2LocalDateTime(time);
        return new TimeProcess(localDateTime);
    }

    public static TimeProcess of(LocalDateTime time) {
        return new TimeProcess(time);
    }

    public Long toLong() {
        return JavaTimeUtil.localDateTime2Long(time);
    }

    public LocalDateTime toLocalDateTime() {
        return time;
    }

    public LocalDate toLocalDate() {
        return toLocalDateTime()
                .toLocalDate();
    }

    public Quarter getQuarter() {
        Month month = time.getMonth();
        int q = JavaTimeUtil.getQuarter(month);
        return new Quarter(time.getYear(),
                q);
    }

    public Long[] getRange(TimeRangeType rangeType) {
        return rangeType.getRangeFunction().apply(time);
    }

    public static void main(String[] args) {
    }
}
