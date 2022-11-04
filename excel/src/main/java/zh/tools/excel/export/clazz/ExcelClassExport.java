package zh.tools.excel.export.clazz;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public interface ExcelClassExport {

    default void run(HttpServletResponse response, Class<?> head, String fileName) throws IOException {
        setRes(response, fileName);
        ServletOutputStream outputStream = response.getOutputStream();
        EasyExcel.write(outputStream, head)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(0, "sheet1")
                .doWrite(contentData());
        outputStream.close();
    }

    List<?> contentData();

    default void setRes(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }
}
