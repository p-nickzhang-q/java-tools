package zh.tools.jpa;

import lombok.Data;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

@Data
public class SpecCompare {

    public Compare compare;

    public SpecCompare(Compare compare) {
        this.compare = compare;
    }

    @FunctionalInterface
    public interface Compare {
        Predicate compare(Path path, Comparable comparable);
    }
}
