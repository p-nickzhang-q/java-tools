package zh.tools.common.filterparse;

import java.util.List;
import java.util.Map;

public interface ParseStrategy {
    void fuzzy(String field, Object value);

    void between(String field, Object value);

    void equal(String field, Object value);

    void greaterEqual(String field, Object value);

    void greaterThan(String field, Object value);

    void lessThan(String field, Object value);

    void lessEqual(String field, Object value);

    void start(String field, String value);

    void end(String field, String value);

    void notEqual(String field, Object value);

    void notNull(String field);

    void nullProcess(String field);

    void parseFilter(Map<String, Object> filter);

    void and(List<Object> values);

    void or(List<Object> values);

    void and(Map<String, Object> value);

    void or(Map<String, Object> value);

    void in(String field, List<Object> values);

    void childEntityProcess(String field, Map<String, Object> value);
}
