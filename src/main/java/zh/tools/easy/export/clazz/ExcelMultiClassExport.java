package zh.tools.easy.export.clazz;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import zh.tools.easy.Util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface ExcelMultiClassExport {
    /*LinkedHashMap有顺序的,这样可以按照添加顺序执行*/
    default void run(HttpServletResponse response, LinkedHashMap<String, MultiSheetConfig> head, String fileName) throws IOException {
        Util.setRes(response, fileName);
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        List<String> sheetNames = new ArrayList<>(head.keySet());
        for (Map.Entry<String, MultiSheetConfig> entry : head.entrySet()) {
            String sheetName = entry.getKey();
            MultiSheetConfig config = entry.getValue();
            int sheetIndex = sheetNames.indexOf(sheetName);
            ExcelWriterSheetBuilder excelWriterSheetBuilder = EasyExcel.writerSheet(sheetIndex, sheetName);
            if (config.getMergeWriteHandler() != null) {
                excelWriterSheetBuilder.registerWriteHandler(config.getMergeWriteHandler());
            }
            WriteSheet sheet = excelWriterSheetBuilder.head(config.getAClass()).build();
            excelWriter.write(config.getContent().get(), sheet);
        }
        excelWriter.finish();
    }

}
