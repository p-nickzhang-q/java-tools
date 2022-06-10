package zh.tools.list;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtil {
    public static <T, R> List<R> getPropertyList(List<T> value, Function<T, R> getProperty) {
        return value
                .stream()
                .filter(Objects::nonNull)
                .map(getProperty)
                .distinct()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> getPropertiesList(List<T> value, Function<T, List<R>> getProperty) {
        return value
                .stream()
                .flatMap(t -> getProperty
                        .apply(t)
                        .stream())
                .distinct()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T, P> void setValueInList(List<T> value, List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, P> setValue) {
        value.forEach(t -> parameters
                .stream()
                .filter(p -> filter.test(t,
                        p))
                .findFirst()
                .ifPresent(p -> {
                    setValue.accept(t,
                            p);
                }));
    }

    @SafeVarargs
    public static <T, P> void setMultiValueInList(List<T> value, List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, P>... setValues) {
        value.forEach(t -> parameters
                .stream()
                .filter(p -> filter.test(t,
                        p))
                .findFirst()
                .ifPresent(p -> Arrays
                        .stream(setValues)
                        .forEach(setValue -> setValue.accept(t,
                                p))));
    }

    public static <T, P> void setValuesInList(List<T> value, List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, List<P>> setValue) {
        value.forEach(t -> {
            List<P> list = parameters
                    .stream()
                    .filter(p -> filter.test(t,
                            p))
                    .collect(Collectors.toList());
            //list不可能为null,如果为空集合,是设属性为空集合是合理的,不然属性为null,反而会报错
            setValue.accept(t,
                    list);
        });
    }

    public static <T> List<T> filter(List<T> value, Predicate<T> filter) {
        return value
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static <T> Optional<T> find(List<T> value, Predicate<T> filter) {
        return value
                .stream()
                .filter(filter)
                .findFirst();
    }

}
