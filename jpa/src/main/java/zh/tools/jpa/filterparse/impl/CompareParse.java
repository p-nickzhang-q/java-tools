package zh.tools.jpa.filterparse.impl;

import zh.tools.common.time.JavaTimeUtil;
import zh.tools.jpa.SpecCompare;
import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;

public class CompareParse extends FilterParse {
    public CompareParse(FilterParser filterParser) {
        super(filterParser);
    }

    protected void processCompare(String field, Object value, SpecCompare compare) {
        Path<?> root = filterParser.getRoot();
        List<Predicate> restrictions = filterParser.getRestrictions();
        Predicate predicate;
        Path<Object> path = root.get(field);
        if (value instanceof String) {
            if (JavaTimeUtil.matchDateReg(value.toString())) {
                predicate = compare.commonCompare.compare(path,
                        JavaTimeUtil.str2LocalDate(value.toString()));
            } else if (JavaTimeUtil.matchDateTimeReg(value.toString())) {
                predicate = compare.commonCompare.compare(path,
                        JavaTimeUtil.str2LocalDateTime(value.toString()));
            } else {
                predicate = compare.pathCompare.compare(path,
                        root.get(value.toString()));
            }
        } else {
            predicate = compare.commonCompare.compare(path,
                    ((Comparable) value));
        }
        restrictions.add(predicate);
    }
}
