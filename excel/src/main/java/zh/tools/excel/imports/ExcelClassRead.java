package zh.tools.excel.imports;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import org.springframework.web.multipart.MultipartFile;
import zh.tools.excel.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExcelClassRead {

    public static <T> List<T> read(MultipartFile file, Class<T> tClass) throws IOException {
        Util.validateExcel(file);
        InputStream inputStream = file.getInputStream();
        return read(tClass, inputStream);
    }

    public static <T> List<T> read(Class<T> tClass, InputStream inputStream) {
        ExcelAnalysisEventListener<T> eventListener = new ExcelAnalysisEventListener<T>(1,
                tClass) {
        };
        EasyExcel
                .read(inputStream,
                        tClass,
                        eventListener)
                .extraRead(CellExtraTypeEnum.MERGE)
                .sheet()
                .doRead();
        /*读取过后有错误就要提前报错*/
        Util.throwError(eventListener);
        return eventListener.getEntities();
    }

    public static <T> T readForm(MultipartFile file, Class<T> tClass) throws IOException {
        Util.validateExcel(file);
        ExcelFormModelDataListener<T> readListener = new ExcelFormModelDataListener<>(tClass);
        EasyExcel
                .read(file.getInputStream(),
                        readListener)
                .ignoreEmptyRow(false)
                .extraRead(CellExtraTypeEnum.MERGE)
                .sheet()
                .headRowNumber(0)
                .doRead();
        Util.throwError(readListener);
        return readListener.getEntity();
    }


}
