package zh.tools.easy.imports;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import org.springframework.web.multipart.MultipartFile;
import zh.tools.error.ZhToolsBaseException;
import zh.tools.list.LambdaUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ExcelClassImport<T, R> {

    // TODO: 2021/8/5 重构classImport, 使用ExcelClassRead
    default List<R> run(MultipartFile file, Class<T> aClass) throws IOException {
        if (!Objects
                .requireNonNull(file.getOriginalFilename())
                .contains(".xlsx")) {
            throw new ZhToolsBaseException("请上传xlsx文件");
        }
        ExcelAnalysisEventListener<T> eventListener = new ExcelAnalysisEventListener<T>(1,
                aClass) {
        };
        EasyExcel
                .read(file.getInputStream(),
                        aClass,
                        eventListener)
                .extraRead(CellExtraTypeEnum.MERGE)
                .sheet()
                .autoTrim(true)
                .doRead();
        process(eventListener);
        /*读取过后有错误就要提前报错*/
        throwError(eventListener);
        List<R> data = eventListener
                .getEntities()
                .stream()
                .filter(this::filter)
                .map(LambdaUtils.functionWithIndex((t, i) -> rowData(t,
                        i + eventListener.getHeadRowNumber(),
                        eventListener)))
                .collect(Collectors.toList());
        /*处理数据报错*/
        throwError(eventListener);
        return data;
    }

    default void throwError(ExcelAnalysisEventListener<T> eventListener) {
        if (eventListener.excelErrorHandler
                .getErrMsg()
                .size() > 0) {
            throw new ZhToolsBaseException(String.join(";",
                    eventListener.excelErrorHandler.getErrMsg()));
        }
    }

    default void process(ExcelAnalysisEventListener<T> eventListener) {

    }

    default boolean filter(T t) {
        return true;
    }

    R rowData(T t, int rowNo, ExcelAnalysisEventListener<T> eventListener);

}
