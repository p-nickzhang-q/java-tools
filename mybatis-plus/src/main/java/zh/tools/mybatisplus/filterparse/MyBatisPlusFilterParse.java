package zh.tools.mybatisplus.filterparse;

import zh.tools.common.filterparse.BaseFilterParse;
import zh.tools.common.filterparse.ParseStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBatisPlusFilterParse extends BaseFilterParse {

    protected final MyBatisPlusStrategy myBatisPlusStrategy;

    public MyBatisPlusFilterParse(MyBatisPlusStrategy myBatisPlusStrategy) {
        this.myBatisPlusStrategy = myBatisPlusStrategy;
    }

    public static String humpToUnderline(String key) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern
                .compile(regex)
                .matcher(key);
        while (matcher.find()) {
            String target = matcher.group();
            key = key.replaceAll(target,
                    "_" + target.toLowerCase());
        }
        return key;
    }

    @Override
    public ParseStrategy getParseStrategy() {
        return myBatisPlusStrategy;
    }
}
