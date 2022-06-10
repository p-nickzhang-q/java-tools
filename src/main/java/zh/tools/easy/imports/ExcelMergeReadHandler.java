package zh.tools.easy.imports;

import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.metadata.CellExtra;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class ExcelMergeReadHandler {

    @Getter
    private final List<CellExtra> extraMergeInfoList = new ArrayList<>();

    public abstract void explainMergeData();

    public void merge() {
        if (!this
                .getExtraMergeInfoList()
                .isEmpty()) {
            explainMergeData();
        }
    }

    public void addMergeData(CellExtra extra) {
        if (extra.getType() == CellExtraTypeEnum.MERGE) {
            this
                    .getExtraMergeInfoList()
                    .add(extra);
        }
    }
}
