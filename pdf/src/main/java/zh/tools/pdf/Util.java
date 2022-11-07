package zh.tools.pdf;

import cn.hutool.core.lang.Console;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

    public static String readPdf(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        try {
            // 读取文本内容
            PDDocument document = PDDocument.load(inputStream);
            // 获取页码
            int pages = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            // 设置按顺序输出
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(pages);
            content.append(stripper.getText(document));
            document.close();
        } catch (Exception e) {
            Console.error(e);
        }
        return content.toString();
    }
}
