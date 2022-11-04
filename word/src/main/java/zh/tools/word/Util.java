package zh.tools.word;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {
    public static String setRes(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        try {
            fileName = URLEncoder
                    .encode(fileName,
                            StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+",
                            "%20");
            response.setHeader("Content-disposition",
                    "attachment;filename*=utf-8''" + fileName + ".docx");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
