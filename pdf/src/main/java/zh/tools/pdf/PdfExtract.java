package zh.tools.pdf;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import zh.tools.common.list.ListUtil;
import zh.tools.common.map.FilterMap;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class PdfExtract {
    /**
     * 中文标点符号
     */
    public static final String ZH_ZF = "[\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b]";
    private String regex;
    private String split;

    public PdfExtract(String regex, String split) {
        this.regex = regex;
        this.split = split;
    }

    public static PdfExtract getDefault(String label) {
        String regex = String.format("%s[：|:]\\s?\\S+",
                label);
        return new PdfExtract(regex,
                "[：|:]");
    }

    public static FilterMap<String, String> extractedInfo(String content, List<PdfExtract> pdfExtracts) {
        FilterMap<String, String> result = new FilterMap<>();
        List<String> strings = cn.hutool.core.collection.ListUtil.toList(content.split("\r\n"));
        for (PdfExtract pdfExtract : pdfExtracts) {
            Predicate<String> predicate = s -> {
                List<String> all = ReUtil.findAll(pdfExtract.getRegex(),
                        s,
                        0);
                boolean match = all.size() > 0;
                if (match) {
                    List<String> list = Arrays
                            .stream(all
                                    .get(0)
                                    .split(pdfExtract.getSplit()))
                            .filter(StrUtil::isNotBlank)
                            .map(String::trim)
                            .map(s1 -> s1.replaceAll(ZH_ZF,
                                    ""))
                            .collect(Collectors.toList());
                    result.put(list.get(0),
                            list.get(1));
                }
                return match;
            };
            ListUtil.find(strings,
                    predicate);
        }
        return result;
    }
}
