package zh.tools.jpa.filterparse.impl;

import zh.tools.common.time.JavaTimeUtil;
import zh.tools.jpa.filterparse.FilterParse;
import zh.tools.jpa.filterparse.FilterParser;

import java.util.List;
import java.util.regex.Pattern;

public class BetweenParse extends FilterParse {

    public final static String DATE_REG = "\\d{4}-\\d{2}-\\d{2}";

    public BetweenParse(FilterParser filterParser) {
        super(filterParser);
    }

    @Override
    public void parse(String field, Object value) {
        Object[] temp;
        if (value instanceof List) {
            temp = ((List) value).toArray();
        } else {
            temp = (Object[]) value;
        }
        if (temp != null && temp.length > 0) {
            if (temp[0] instanceof String) {
                if (Pattern.matches(DATE_REG,
                        temp[0].toString())) {
                    temp[0] = JavaTimeUtil.str2LocalDate(temp[0].toString());
                    temp[1] = JavaTimeUtil.str2LocalDate(temp[1].toString());
                } else {
                    temp[0] = JavaTimeUtil.str2LocalDateTime(temp[0].toString());
                    temp[1] = JavaTimeUtil.str2LocalDateTime(temp[1].toString());
                }
            }
            filterParser.addRestriction((root, criteriaBuilder) -> criteriaBuilder.between(root.get(field),
                    (Comparable) temp[0],
                    (Comparable) temp[1]));
        }
    }
}
