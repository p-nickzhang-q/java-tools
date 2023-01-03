package zh.tools.jpa.filterparse;

import lombok.Getter;
import zh.tools.jpa.FilterParser;

@Getter
public abstract class FilterParse {
    protected final FilterParser filterParser;

    public FilterParse(FilterParser filterParser) {
        this.filterParser = filterParser;
    }

    public abstract void parse(String field, Object value);

}
