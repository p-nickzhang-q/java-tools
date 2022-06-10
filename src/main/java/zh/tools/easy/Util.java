package zh.tools.easy;

import org.springframework.web.multipart.MultipartFile;
import zh.tools.easy.imports.ExcelAnalysisEventListener;
import zh.tools.easy.imports.ExcelFormModelDataListener;
import zh.tools.error.ZhToolsBaseException;

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
            throw new ZhToolsBaseException("请上传xlsx文件");
        }
    }

    public static <T> void throwError(ExcelAnalysisEventListener<T> eventListener) {
        if (eventListener.excelErrorHandler
                .getErrMsg()
                .size() > 0) {
            throw new ZhToolsBaseException(String.join(";",
                    eventListener.excelErrorHandler.getErrMsg()));
        }
    }

    public static <T> void throwError(ExcelFormModelDataListener<T> eventListener) {
        if (eventListener.excelErrorHandler
                .getErrMsg()
                .size() > 0) {
            throw new ZhToolsBaseException(String.join(";",
                    eventListener.excelErrorHandler.getErrMsg()));
        }
    }
}
