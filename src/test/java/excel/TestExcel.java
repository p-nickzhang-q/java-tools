package excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TestExcel {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("部门")
    private String depart;
}
