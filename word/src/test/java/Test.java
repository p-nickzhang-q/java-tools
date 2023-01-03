import cn.hutool.core.io.FileUtil;
import zh.tools.common.map.FilterMap;
import zh.tools.word.WordFillExport;

import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        FilterMap<String, Object> filterMap = FilterMap
                .newFilterMap()
                .wrapList("list",
                        filterMaps -> {
                            filterMaps.add(FilterMap
                                    .newFilterMap()
                                    .set("name",
                                            "A")
                                    .wrapList("list",
                                            filterMaps1 -> {
                                                filterMaps1.add(FilterMap
                                                        .newFilterMap()
                                                        .set("date",
                                                                "11/11")
                                                        .set("hour",
                                                                "0.5")
                                                        .set("item",
                                                                "")
                                                        .set("amount",
                                                                "1250"));
                                                filterMaps1.add(FilterMap
                                                        .newFilterMap()
                                                        .set("date",
                                                                "11/11")
                                                        .set("hour",
                                                                "0.5")
                                                        .set("item",
                                                                "")
                                                        .set("amount",
                                                                "1250"));
                                                filterMaps1.add(FilterMap
                                                        .newFilterMap()
                                                        .set("date",
                                                                "11/11")
                                                        .set("hour",
                                                                "0.5")
                                                        .set("item",
                                                                "")
                                                        .set("amount",
                                                                "1250"));
                                            }));
                        });
        WordFillExport export = () -> filterMap;
        try {
            export.run(FileUtil.getInputStream("D:/Gitee/java-tools/word/src/test/resources/test.docx"),
                    FileUtil.getOutputStream("D:/Gitee/java-tools/word/src/test/resources/test-out.docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
