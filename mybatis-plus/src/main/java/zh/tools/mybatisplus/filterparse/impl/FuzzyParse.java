package zh.tools.mybatisplus.filterparse.impl;

import zh.tools.mybatisplus.filterparse.MyBatisPlusFilterParse;
import zh.tools.mybatisplus.filterparse.MyBatisPlusStrategy;

public class FuzzyParse extends MyBatisPlusFilterParse {
    public FuzzyParse(MyBatisPlusStrategy myBatisPlusStrategy) {
        super(myBatisPlusStrategy);
    }

    @Override
    public void parse(String field, Object value) {
        String likeString = value
                .toString()
                .replaceAll("%",
                        "");
        /*like会自动添加%,所以要去%*/
        myBatisPlusStrategy
                .getQueryWrapper()
                .like(field,
                        likeString);
    }
}
