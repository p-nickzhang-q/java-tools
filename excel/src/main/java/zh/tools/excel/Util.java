package zh.tools.excel;

import cn.hutool.core.exceptions.ValidateException;
import org.springframework.web.multipart.MultipartFile;
import zh.tools.excel.imports.ExcelAnalysisEventListener;
import zh.tools.excel.imports.ExcelFormModelDataListener;


import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

public class Util {
    public static void setRes(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileName = URLEncoder
                .encode(fileName,
                        "UTF-8")
                .replaceAll("\\+",
                        "%20");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    public static void validateExcel(MultipartFile file) {
        if (!Objects
                .requireNonNull(file.getOriginalFilename())
                .contains(".xlsx")) {
            throw new ValidateException("请上传xlsx文件");
        }
    }

    public static <T> void throwError(ExcelAnalysisEventListener<T> eventListener) {
        if (eventListener.excelErrorHandler
                .getErrMsg()
                .size() > 0) {
            throw new ValidateException(String.join(";",
                    eventListener.excelErrorHandler.getErrMsg()));
        }
    }

    public static <T> void throwError(ExcelFormModelDataListener<T> eventListener) {
        if (eventListener.excelErrorHandler
                .getErrMsg()
                .size() > 0) {
            throw new ValidateException(String.join(";",
                    eventListener.excelErrorHandler.getErrMsg()));
        }
    }
}
