package zh.tools.jpa;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class SpecCompare {

    public LocalDateCompare localDateCompare;
    public LocalDateTimeCompare localDateTimeCompare;
    public PathCompare pathCompare;
    public NumCompare numCompare;
    public DateCompare dateCompare;

    public SpecCompare(LocalDateCompare localDateCompare, LocalDateTimeCompare localDateTimeCompare, PathCompare pathCompare, NumCompare numCompare) {
        this.localDateCompare = localDateCompare;
        this.localDateTimeCompare = localDateTimeCompare;
        this.pathCompare = pathCompare;
        this.numCompare = numCompare;
    }

    public SpecCompare(LocalDateCompare localDateCompare, LocalDateTimeCompare localDateTimeCompare, PathCompare pathCompare, NumCompare numCompare, DateCompare dateCompare) {
        this.localDateCompare = localDateCompare;
        this.localDateTimeCompare = localDateTimeCompare;
        this.pathCompare = pathCompare;
        this.numCompare = numCompare;
        this.dateCompare = dateCompare;
    }

    @FunctionalInterface
    public interface LocalDateCompare {
        Predicate compare(Path path, LocalDate localDate);
    }

    @FunctionalInterface
    public interface DateCompare {
        Predicate compare(Path path, Date date);
    }

    @FunctionalInterface
    public interface LocalDateTimeCompare {
        Predicate compare(Path path, LocalDateTime localDateTime);
    }

    @FunctionalInterface
    public interface PathCompare {
        Predicate compare(Path path, Path path2);
    }

    @FunctionalInterface
    public interface NumCompare {
        Predicate compare(Path path, Number number);
    }
}
