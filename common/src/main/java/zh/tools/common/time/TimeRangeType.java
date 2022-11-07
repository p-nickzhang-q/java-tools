package zh.tools.common.time;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Function;

@Getter
public enum TimeRangeType {
    MINUTE((localDateTime) -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime.toLocalDate(),
                LocalTime.of(localDateTime.getHour(),
                        localDateTime.getMinute()));
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.MINUTES
                .getDuration()
                .toMillis() - 1;
        return range;
    }), HOUR(localDateTime -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime.toLocalDate(),
                LocalTime.of(localDateTime.getHour(),
                        0));
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.HOURS
                .getDuration()
                .toMillis() - 1;
        return range;
    }), DAY(localDateTime -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime.toLocalDate(),
                LocalTime.MIN);
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.DAYS
                .getDuration()
                .toMillis() - 1;
        return range;
    }), WEEK(localDateTime -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime
                        .toLocalDate()
                        .with(DayOfWeek.SUNDAY),
                LocalTime.MIN);
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.WEEKS
                .getDuration()
                .toMillis() - 1;
        return range;
    }), MONTH(localDateTime -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime
                        .toLocalDate()
                        .with(TemporalAdjusters.firstDayOfMonth()),
                LocalTime.MIN);
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.MONTHS
                .getDuration()
                .toMillis() - 1;
        return range;
    }), QUARTER(localDateTime -> {
        return JavaTimeUtil.quarterRange(YearMonth.from(localDateTime));
    }), HALF_YEAR(localDateTime -> {
        return JavaTimeUtil.halfYearRange(YearMonth.from(localDateTime));
    }), YEAR(localDateTime -> {
        Long[] range = new Long[2];
        LocalDateTime temp = LocalDateTime.of(localDateTime
                        .toLocalDate()
                        .with(TemporalAdjusters.firstDayOfYear()),
                LocalTime.MIN);
        range[0] = TimeProcess
                .of(temp)
                .toLong();
        range[1] = range[0] + ChronoUnit.YEARS
                .getDuration()
                .toMillis() - 1;
        return range;
    });

    private final Function<LocalDateTime, Long[]> rangeFunction;

    TimeRangeType(Function<LocalDateTime, Long[]> rangeFunction) {
        this.rangeFunction = rangeFunction;
    }
}
