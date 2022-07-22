package excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CleanHouse {
    @ExcelProperty("清洁屋位置")
    private String address;
    @ExcelProperty("属地")
    private String street;
    @ExcelProperty("所属社区")
    private String sq;
    @ExcelProperty("小区名称")
    private String xq;
    @ExcelProperty("负责人")
    private String person_in_charge;
    @ExcelProperty("联系电话")
    private String phone;
    @ExcelProperty("物业公司")
    private String property;
    @ExcelProperty("冬季")
    private String winter_open_time;
    @ExcelProperty("夏季")
    private String summer_open_time;
    @ExcelProperty("类型")
    private String type;

    private String types;
}
