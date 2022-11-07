package zh.tools.common.time;




import cn.hutool.core.exceptions.ValidateException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class JavaTimeUtil {

    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime
            .now()
            .getOffset();

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static int getQuarter(Month month) {
        int value = month.getValue();
        if (value <= 3) {
            return 1;
        } else if (value <= 6) {
            return 2;
        } else if (value <= 9) {
            return 3;
        } else {
            return 4;
        }
    }

    public static Long[] quarterRange(YearMonth yearMonth) {
        Month firstMonthOfQuarter = yearMonth
                .getMonth()
                .firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);
        return getLongs(yearMonth,
                firstMonthOfQuarter,
                endMonthOfQuarter);
    }

    public static Long[] halfYearRange(YearMonth yearMonth) {
        boolean firstHalfYear = isFirstHalfYear(yearMonth);
        Month startMonth, endMonth;
        if (firstHalfYear) {
            startMonth = Month.JANUARY;
            endMonth = Month.JUNE;
        } else {
            startMonth = Month.JULY;
            endMonth = Month.DECEMBER;
        }
        return getLongs(yearMonth,
                startMonth,
                endMonth);
    }

    public static boolean isFirstHalfYear(YearMonth yearMonth) {
        return yearMonth
                .getMonth()
                .getValue() <= 6;
    }

    public static Long[] getLongs(YearMonth yearMonth, Month startMonth, Month endMonth) {
        LocalDate date = LocalDate.of(yearMonth.getYear(),
                startMonth,
                1);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime endTime = LocalDate
                .of(yearMonth.getYear(),
                        endMonth,
                        endMonth.length(date.isLeapYear()))
                .atTime(LocalTime.MAX);
        return new Long[]{localDateTime2Long(start), localDateTime2Long(endTime)};
    }

    public static String milli2DateStr(long milli) {
        if (milli == 0) {
            return "";
        }
        return long2LocalDateTime(milli)
                .format(DATE_FORMATTER);
    }

    public static String milli2DateTimeStr(long milli) {
        if (milli == 0) {
            return "";
        }
        return long2LocalDateTime(milli)
                .format(DATE_TIME_FORMATTER);
    }

    public static String milli2Str(long milli, String format) {
        if (milli == 0) {
            return "";
        }
        return long2LocalDateTime(milli)
                .format(DateTimeFormatter.ofPattern(format));
    }

    public static String localDate2Str(LocalDate localDate) {
        return localDate.format(DATE_FORMATTER);
    }

    public static String localDate2Str(LocalDate localDate, String s) {
        return localDate.format(DateTimeFormatter.ofPattern(s));
    }

    public static String localDateTime2Str(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    public static long until(long startDate, long endDate, ChronoUnit unit) {
        long seconds = long2LocalDateTime(startDate)
                .until(long2LocalDateTime(endDate),
                        ChronoUnit.SECONDS);
        //不足一天的算一天
        return BigDecimal
                .valueOf(seconds)
                .divide(BigDecimal.valueOf(unit
                                .getDuration()
                                .get(ChronoUnit.SECONDS)),
                        RoundingMode.UP)
                .longValue();
    }

    public static List<LocalDate> getLocalDatesBetween(long startDate, long endDate) {
        long daysBetween = until(startDate,
                endDate,
                ChronoUnit.DAYS);
        List<LocalDate> localDates = new ArrayList<>();
        for (long i = 0; i < daysBetween; i++) {
            localDates.add(long2LocalDate(startDate)
                    .plusDays(i));
        }
        return localDates;
    }

    public static LocalDate str2LocalDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr,
                DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate str2LocalDate(String dateStr) {
        return LocalDate.parse(dateStr);
    }

    public static LocalDateTime str2LocalDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr,
                DATE_TIME_FORMATTER);
    }

    public static Date strTime2Date(String str) {
        LocalDateTime localDateTime = str2LocalDateTime(str);
        return Date.from(localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Date strDate2Date(String str) {
        LocalDate localDate = str2LocalDate(str);
        return Date.from(localDate
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Date strDate2Date(String str, String pattern) {
        LocalDate localDate = str2LocalDate(str,
                pattern);
        return Date.from(localDate
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Long[] getRange(LocalDate current, ChronoUnit unit) {
        long end;
        long start;
        if (unit == ChronoUnit.DAYS) {
            end = getDayStartOrEnd(current,
                    LocalTime.MAX);
            start = getDayStartOrEnd(current,
                    LocalTime.MIN);
        } else if (unit == ChronoUnit.MONTHS) {
            start = getDayStartOrEnd(current.withDayOfMonth(1),
                    LocalTime.MIN);
            end = getDayStartOrEnd(current.with(TemporalAdjusters.lastDayOfMonth()),
                    LocalTime.MAX);
        } else if (unit == ChronoUnit.YEARS) {
            start = getDayStartOrEnd(current.withDayOfYear(1),
                    LocalTime.MIN);
            end = getDayStartOrEnd(current.with(TemporalAdjusters.lastDayOfYear()),
                    LocalTime.MAX);
        } else {
            throw new ValidateException("不支持" + unit);
        }
        return new Long[]{start, end};
    }

    public static long getDayStartOrEnd(LocalDate current, LocalTime time) {
        return current
                .atStartOfDay()
                .with(time)
                .toInstant(ZONE_OFFSET)
                .toEpochMilli();
    }

    public static Long[] getRange(String date, ChronoUnit unit) {
        LocalDate localDate = str2LocalDate(date);
        return getRange(localDate,
                unit);
    }

    public static long localDateTime2Long(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        return localDateTime
                .toInstant(ZONE_OFFSET)
                .toEpochMilli();
    }

    public static long localDate2Long(LocalDate localDate) {
        if (localDate == null) {
            return 0;
        }
        return localDateTime2Long(LocalDateTime.of(localDate,
                LocalTime.MIN));
    }

    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        long milli = localDateTime
                .toInstant(ZONE_OFFSET)
                .toEpochMilli();
        return new Date(milli);
    }

    public static String date2Str(Date date) {
        if (date == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = date
                .toInstant()
                .atZone(ZONE_OFFSET);
        return localDateTime2Str(zonedDateTime.toLocalDateTime());
    }

    public static LocalDate date2LocalDate(Date date) {
        if (date == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = date
                .toInstant()
                .atZone(ZONE_OFFSET);
        return zonedDateTime.toLocalDate();
    }

    public static long now() {
        return LocalDateTime
                .now()
                .toInstant(ZONE_OFFSET)
                .toEpochMilli();
    }

    public static LocalDate long2LocalDate(long time) {
        ZonedDateTime zonedDateTime = new Date(time)
                .toInstant()
                .atZone(ZoneId.systemDefault());
        return zonedDateTime.toLocalDate();
    }

    public static Date long2Date(long time) {
        return new Date(time);
    }

    public static YearMonth long2YearMonth(long time) {
        ZonedDateTime zonedDateTime = new Date(time)
                .toInstant()
                .atZone(ZoneId.systemDefault());
        return YearMonth.from(zonedDateTime);
    }

    public static LocalDateTime long2LocalDateTime(long time) {
        ZonedDateTime zonedDateTime = new Date(time)
                .toInstant()
                .atZone(ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalTime long2LocalTime(long time) {
        ZonedDateTime zonedDateTime = new Date(time)
                .toInstant()
                .atZone(ZoneId.systemDefault());
        return zonedDateTime.toLocalTime();
    }

    public static ZonedDateTime calendar2ZonedDateTime(Calendar calendar) {
        TimeZone timeZone = calendar.getTimeZone();
        ZoneId zoneId = timeZone.toZoneId();
        return ZonedDateTime.ofInstant(calendar.toInstant(),
                zoneId);
    }

    public static LocalDate[] yearMonthRange(YearMonth yearMonth) {
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LocalDate startOfMonth = yearMonth.atDay(1);
        return new LocalDate[]{endOfMonth, startOfMonth};
    }

    public static Long[] yearMonthLongRange(YearMonth yearMonth) {
        LocalDateTime endOfMonth = LocalDateTime.of(yearMonth.atEndOfMonth(),
                LocalTime.MAX);
        LocalDateTime startOfMonth = yearMonth
                .atDay(1)
                .atStartOfDay();
        return new Long[]{localDateTime2Long(startOfMonth), localDateTime2Long(endOfMonth)};
    }

    public static Long[] yearMonthLongRange(YearMonth[] yearMonths) {
        LocalDateTime endOfMonth = LocalDateTime.of(yearMonths[1].atEndOfMonth(),
                LocalTime.MAX);
        LocalDateTime startOfMonth = yearMonths[0]
                .atDay(1)
                .atStartOfDay();
        return new Long[]{localDateTime2Long(startOfMonth), localDateTime2Long(endOfMonth)};
    }


    public static Long[] yearLongRange(Integer year) {
        LocalDateTime endOfMonth = LocalDateTime.of(YearMonth
                        .of(year,
                                12)
                        .atEndOfMonth(),
                LocalTime.MAX);
        LocalDateTime startOfMonth = YearMonth
                .of(year,
                        1)
                .atDay(1)
                .atStartOfDay();
        return new Long[]{localDateTime2Long(startOfMonth), localDateTime2Long(endOfMonth)};
    }

    public static Boolean matchDateTimeReg(String time) {
        try {
            LocalDateTime.parse(time,
                    DATE_TIME_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean matchDateReg(String date) {
        try {
            LocalDate.parse(date,
                    DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSomeTimeInSomeDay(long time, LocalDate date) {
        return long2LocalDateTime(time)
                .toLocalDate()
                .equals(date);
    }

    public static int getDayCount(YearMonth yearMonth) {
        return LocalDate
                .now()
                .withMonth(yearMonth.getMonthValue())
                .lengthOfMonth();
    }

}
