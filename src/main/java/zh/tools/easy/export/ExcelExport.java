package zh.tools.easy.export;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.commons.compress.utils.Lists;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public interface ExcelExport {

    default void run(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileName = URLEncoder
                .encode(fileName,
                        "UTF-8")
                .replaceAll("\\+",
                        "%20");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + fileName + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        List<List<String>> head = head();
        EasyExcel
                .write(outputStream)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(0,
                        fileName)
                .head(head)
                .registerWriteHandler(registerWriteHandler())
                .doWrite(contentData(head));
        outputStream.close();
    }

    List<List<String>> head();

    List<List<Object>> contentData(List<List<String>> heads);

    default AbstractColumnWidthStyleStrategy registerWriteHandler() {
        return new AbstractColumnWidthStyleStrategy() {

        };
    }

    static ArrayList<Object> init(int size) {
        ArrayList<Object> objects = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
            objects.add("");
        }
        return objects;
    }

}
