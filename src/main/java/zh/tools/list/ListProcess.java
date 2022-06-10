package zh.tools.list;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;

public class ListProcess<T> {
    private final List<T> list;

    private ListProcess(List<T> list) {
        this.list = list;
    }

    public static <T> ListProcess<T> of(List<T> list) {
        return new ListProcess<>(list);
    }

    @SafeVarargs
    public static <T> ListProcess<T> of(T... array) {
        return new ListProcess<>(Arrays
                .stream(array)
                .collect(Collectors.toList()));
    }

    public <R> ListProcess<R> getDistinctPropertyList(Function<T, R> getProperty) {
        return of(ListUtil.getPropertyList(list,
                getProperty));
    }

    public <R> ListProcess<R> getDistinctPropertiesList(Function<T, List<R>> getProperty) {
        return of(ListUtil.getPropertiesList(list,
                getProperty));
    }

    public <P> void setValueInList(List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, P> setValue) {
        ListUtil.setValueInList(list,
                parameters,
                filter,
                setValue);
    }

    @SafeVarargs
    public final <P> void setMultiValueInList(List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, P>... setValues) {
        ListUtil.setMultiValueInList(list,
                parameters,
                filter,
                setValues);
    }

    public <P> void setValuesInList(List<P> parameters, BiPredicate<T, P> filter, BiConsumer<T, List<P>> setValue) {
        ListUtil.setValuesInList(list,
                parameters,
                filter,
                setValue);
    }

    public ListProcess<T> filter(Predicate<T> filter) {
        return of(ListUtil.filter(list,
                filter));
    }

    public Optional<T> find(Predicate<T> filter) {
        return ListUtil.find(list,
                filter);
    }

    public BigDecimal sum(Function<T, BigDecimal> map) {
        return list
                .stream()
                .map(map)
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public int sum(ToIntFunction<T> map) {
        return list
                .stream()
                .mapToInt(map)
                .sum();
    }

    public BigDecimal max(Function<T, BigDecimal> map) {
        return list
                .stream()
                .map(map)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal min(Function<T, BigDecimal> map) {
        return list
                .stream()
                .map(map)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public List<T> getList() {
        return list;
    }

    public BigDecimal avg(Function<T, BigDecimal> map) {
        if (list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(map)
                .divide(BigDecimal.valueOf(list.size()),
                        1,
                        RoundingMode.HALF_UP);
    }
}
