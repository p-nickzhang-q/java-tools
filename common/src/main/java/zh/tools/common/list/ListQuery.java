package zh.tools.common.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListQuery<T> {
    private List<T> list;
    private final List<Query> queries = new ArrayList<>();

    private Boolean tempOr = false;

    private class Query {

        Predicate<T> predicate;

        Boolean isOr = false;

        public Query(Predicate<T> predicate) {
            this.predicate = predicate;
        }
    }

    public ListQuery(List<T> list) {
        this.list = list;
    }


    public ListQuery<T> eq(Function<T, Object> function, Object value) {
        Query query = new Query(t -> Objects.equals(function.apply(t), value));
        tempOrProcess(query);
        queries.add(query);
        return this;
    }

    private void tempOrProcess(Query query) {
        if (tempOr) {
            query.isOr = true;
            tempOr = false;
        }
    }

    public ListQuery<T> or() {
        tempOr = true;
        return this;
    }

    public List<T> result() {
        return list.stream().filter(t -> {
            Predicate<T> temp = o -> true;
            for (Query query : queries) {
                if (query.isOr) {
                    temp = temp.or(query.predicate);
                } else {
                    temp = temp.and(query.predicate);
                }
            }
            return temp.test(t);
        }).collect(Collectors.toList());
    }
}
