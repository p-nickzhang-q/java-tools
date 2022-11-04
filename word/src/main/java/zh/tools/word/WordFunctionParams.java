package zh.tools.word;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakClear;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordFunctionParams {

    @FunctionalInterface
    public interface Process<T> {
        void run(XWPFParagraph xwpfParagraph, String allString, String pattern, T value, CTRPr ctrPr);
    }

    public static Process<String> stringProcess = (xwpfParagraph, allString, pattern, o, ctrPr) -> {
        allString = allString.replace(pattern, o);
        createXwpfRun(xwpfParagraph, ctrPr).setText(allString);
    };

    public static Process<Object> newLineProcess = (xwpfParagraph, allString, pattern, o, ctrPr) -> {
        createXwpfRun(xwpfParagraph, ctrPr).addBreak(BreakClear.ALL);
    };

    public static Process<Picture> imageProcess = (xwpfParagraph, allString, pattern, picture, ctrPr) -> {
        try {
            createXwpfRun(xwpfParagraph, ctrPr).addPicture(
                    picture.getInputStream(),
                    picture.getPictureType(),
                    picture.getFilename(),
                    Units.toEMU(picture.getWidth()),
                    Units.toEMU(picture.getHeight())
            );
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }
    };

    public static XWPFRun createXwpfRun(XWPFParagraph xwpfParagraph, CTRPr copyRPr) {
        XWPFRun xwpfRun = xwpfParagraph.createRun();
        if (copyRPr != null) {
            xwpfRun
                    .getCTR()
                    .setRPr(copyRPr);
        }
        return xwpfRun;
    }

    @Getter
    @AllArgsConstructor
    public static class WordFunctionParam<T> {
        private final Process<T> process;
        private final T value;
    }

    @Getter
    @AllArgsConstructor
    public static class Picture {
        private final InputStream inputStream;
        private final int pictureType;
        private final String filename;
        private final int width;
        private final int height;

        public enum PictureType {
            PICTURE_TYPE_EMF(Document.PICTURE_TYPE_EMF),
            PICTURE_TYPE_WMF(Document.PICTURE_TYPE_WMF),
            PICTURE_TYPE_PICT(Document.PICTURE_TYPE_PICT),
            PICTURE_TYPE_JPEG(Document.PICTURE_TYPE_JPEG),
            PICTURE_TYPE_PNG(Document.PICTURE_TYPE_PNG),
            PICTURE_TYPE_DIB(Document.PICTURE_TYPE_DIB),
            PICTURE_TYPE_GIF(Document.PICTURE_TYPE_GIF),
            PICTURE_TYPE_TIFF(Document.PICTURE_TYPE_TIFF),
            PICTURE_TYPE_EPS(Document.PICTURE_TYPE_EPS),
            PICTURE_TYPE_BMP(Document.PICTURE_TYPE_BMP),
            PICTURE_TYPE_WPG(Document.PICTURE_TYPE_WPG);
            @Getter
            private final int value;

            PictureType(int value) {
                this.value = value;
            }

            public static PictureType fromName(String name) {
                String extension = getExtension(name);
                return PictureType.valueOf(String.format("PICTURE_TYPE_%s", extension.toUpperCase(Locale.ROOT)));
            }

            public static String getExtension(String fileName) {
                int lastPoint = fileName.lastIndexOf(".");
                return fileName.substring(lastPoint + 1);
            }


            public static void main(String[] args) {
                System.out.println(fromName("1.png"));
            }
        }

        public Picture(InputStream inputStream, String filename, int width, int height) {
            this.inputStream = inputStream;
            this.filename = filename;
            this.width = width;
            this.height = height;
            this.pictureType = PictureType
                    .fromName(filename).getValue();
        }
    }

    @Getter
    private final List<WordFunctionParam<?>> params = new ArrayList<>();

    public static WordFunctionParams create() {
        return new WordFunctionParams();
    }

    public <T> WordFunctionParams add(Process<T> process, T o) {
        params.add(new WordFunctionParam<>(process, o));
        return this;
    }

    public WordFunctionParams add(Process<Object> process) {
        return add(process, null);
    }

}
