package zh.tools.easy.export.clazz;

import com.alibaba.excel.write.handler.CellWriteHandler;
import lombok.Getter;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class MultiSheetConfig {
    private final Class<?> aClass;
    private final Supplier<List<?>> content;
    private CellWriteHandler mergeWriteHandler;

    public MultiSheetConfig(Class<?> aClass, Supplier<List<?>> content) {
        this.aClass = aClass;
        this.content = content;
    }

    public MultiSheetConfig(Class<?> aClass, Supplier<List<?>> content, CellWriteHandler mergeWriteHandler) {
        this.aClass = aClass;
        this.content = content;
        this.mergeWriteHandler = mergeWriteHandler;
    }
}
