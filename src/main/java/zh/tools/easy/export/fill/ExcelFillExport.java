package zh.tools.easy.export.fill;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import zh.tools.easy.Object2Map;
import zh.tools.easy.Util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ExcelFillExport {


    default void run(HttpServletResponse response, String templateFileName, String fileName) throws IOException {
        InputStream inputStream = this
                .getClass()
                .getResourceAsStream(templateFileName);
        run(response,
                inputStream,
                fileName,
                0);
    }

    default void run(HttpServletResponse response, InputStream inputStream, String fileName) throws IOException {
        /*easyExcel无法读取多层数据,所以讲多层数据转化为一层*/
        Util.setRes(response,
                fileName);
        Object data = getData();
        Map<String, Object> map = new Object2Map("-",
                data).run();
        ServletOutputStream outputStream = response.getOutputStream();
        EasyExcel
                .write(outputStream)
                .excelType(ExcelTypeEnum.XLSX)
                .withTemplate(inputStream)
                .sheet()
                .doFill(map);
        outputStream.close();
    }

    //用于填充数组数据
    default void run(HttpServletResponse response, InputStream inputStream, String fileName, int sheetNo) throws IOException {
        Util.setRes(response,
                fileName);
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel
                    .write(outputStream)
                    .excelType(ExcelTypeEnum.XLSX)
                    .withTemplate(inputStream)
                    .build();
            if (sheetConfig()
                    .isEmpty()) {
                fillSheet(sheetNo,
                        excelWriter,
                        getListData(),
                        getData(),
                        WriteDirectionEnum.VERTICAL);
            } else {
                for (SheetFillExportConfig sheetFillExportConfig : sheetConfig()) {
                    fillSheet(sheetFillExportConfig.getSheetNo(),
                            excelWriter,
                            sheetFillExportConfig
                                    .getListData()
                                    .get(),
                            sheetFillExportConfig
                                    .getData()
                                    .get(),
                            sheetFillExportConfig.getDirection());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    default void fillSheet(int sheetNo, ExcelWriter excelWriter, List<?> listData, Object data, WriteDirectionEnum direction) {
        //'.'是easyExcel中的关键符号,所以不能用'.'
        Map<String, Object> map = new Object2Map("-",
                data).run();
        WriteSheet writeSheet = EasyExcel
                .writerSheet(sheetNo)
                .build();
        FillConfig fillConfig = FillConfig
                .builder()
//                .direction(direction)
                .forceNewRow(isInsertNew())
                .build();
        excelWriter.fill(listData
                        .stream()
                        .map(o -> new Object2Map("-",
                                o).run())
                        .collect(Collectors.toList()),
                fillConfig,
                writeSheet);
        if (!map.isEmpty()) {
            excelWriter.fill(map,
                    writeSheet);
        }
    }

    default void run(HttpServletResponse response, String templateFileName, String fileName, int sheetNo) throws IOException {
        InputStream inputStream = this
                .getClass()
                .getResourceAsStream(templateFileName);
        run(response,
                inputStream,
                fileName,
                sheetNo);
    }

    default Object getData() {
        return new HashMap<>();
    }

    default List<?> getListData() {
        return new ArrayList<>();
    }

    default boolean isInsertNew() {
        return false;
    }

    default List<SheetFillExportConfig> sheetConfig() {
        return new ArrayList<>();
    }
}
