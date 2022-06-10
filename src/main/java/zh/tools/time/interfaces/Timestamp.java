package zh.tools.time.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import zh.tools.time.HalfYear;
import zh.tools.time.JavaTimeUtil;
import zh.tools.time.Quarter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public interface Timestamp {
    @JsonIgnore
    Long getTimestamp();

    void setTimestamp(Long timestamp);

    default String timestampPatternStr(String pattern) {
        return getLocalDateTime()
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    default boolean isDayTime(int dayHourStart, int dayHourEnd) {
        int hour = getLocalDateTime()
                .getHour();
        return hour >= dayHourStart && hour < dayHourEnd;
    }

    default boolean isNightTime(int dayHourStart, int dayHourEnd) {
        int hour = getLocalDateTime()
                .getHour();
        return hour < dayHourStart || hour >= dayHourEnd;
    }

    @JsonIgnore
    default LocalDateTime getLocalDateTime() {
        return JavaTimeUtil.long2LocalDateTime(getTimestamp());
    }

    @JsonIgnore
    default YearMonth getYearMonth() {
        LocalDate localDate = getLocalDateTime()
                .toLocalDate();
        return YearMonth.from(localDate);
    }

    default int getMonth() {
        return getYearMonth()
                .getMonthValue();
    }

    @JsonIgnore
    default Quarter getQuarter() {
        YearMonth yearMonth = getYearMonth();
        int quarter = JavaTimeUtil.getQuarter(Month.of(yearMonth.getMonthValue()));
        return new Quarter(yearMonth.getYear(),
                quarter);
    }

    default HalfYear halfYear() {
        YearMonth yearMonth = getYearMonth();
        int quarter = JavaTimeUtil.getQuarter(Month.of(yearMonth.getMonthValue()));
        return new HalfYear(yearMonth.getYear(),
                quarter <= 2);
    }

    default boolean inSomeDay(LocalDate date) {
        return getLocalDateTime()
                .toLocalDate()
                .equals(date);
    }

    default boolean inSomeMonth(YearMonth yearMonth) {
        LocalDate localDate = getLocalDateTime()
                .toLocalDate();
        return yearMonth.equals(YearMonth.of(localDate.getYear(),
                localDate.getMonth()));
    }
}
