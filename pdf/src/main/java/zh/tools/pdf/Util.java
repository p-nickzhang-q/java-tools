package zh.tools.pdf;

import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

    public static void htmlToPdfReturnProcess(HttpServletResponse response, String fileName, InputStream pdf) throws IOException {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        fileName = URLEncoder
                .encode(fileName,
                        StandardCharsets.UTF_8.toString())
                .replaceAll("\\+",
                        "%20");
        String s1 = String.format("attachment; filename*=UTF-8''%s.pdf",
                fileName);
        response.setHeader("Content-Disposition",
                s1);
        StreamUtils.copy(pdf,
                response.getOutputStream());
    }

}
