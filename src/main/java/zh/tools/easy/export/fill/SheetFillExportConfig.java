package zh.tools.easy.export.fill;

import com.alibaba.excel.enums.WriteDirectionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SheetFillExportConfig {
    private int sheetNo;
    private Supplier<Object> data = HashMap::new;
    private Supplier<List<?>> listData;
    private WriteDirectionEnum direction = WriteDirectionEnum.VERTICAL;
    private boolean isInsertNew = false;

    public SheetFillExportConfig(int sheetNo, Supplier<List<?>> listData) {
        this.sheetNo = sheetNo;
        this.listData = listData;
    }

    public SheetFillExportConfig(int sheetNo, Supplier<Object> data, Supplier<List<?>> listData) {
        this.sheetNo = sheetNo;
        this.data = data;
        this.listData = listData;
    }

    public SheetFillExportConfig(int sheetNo, Supplier<Object> data, Supplier<List<?>> listData, WriteDirectionEnum direction) {
        this.sheetNo = sheetNo;
        this.data = data;
        this.listData = listData;
        this.direction = direction;
    }
}
