package zh.tools.jpa;

import lombok.Data;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

@Data
public class SpecCompare {

    public Compare<Comparable> commonCompare;
    public Compare<Path> pathCompare;

    public SpecCompare(Compare<Comparable> commonCompare) {
        this.commonCompare = commonCompare;
    }

    public SpecCompare(Compare<Comparable> commonCompare, Compare<Path> pathCompare) {
        this.commonCompare = commonCompare;
        this.pathCompare = pathCompare;
    }

    @FunctionalInterface
    public interface Compare<T> {
        Predicate compare(Path path, T comparable);
    }
}
