package zh.tools.excel.imports;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import zh.tools.excel.annotation.ExcelForm;
import zh.tools.excel.annotation.ExcelFormField;
import zh.tools.excel.annotation.ExcelValidate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 表单类型读取Excel
 */
@Slf4j
public class ExcelFormModelDataListener<T> extends AnalysisEventListener<Map<Integer, Object>> {

    Map<Integer, Map<Integer, Object>> data = new HashMap<>();
    private final Class<T> aClass;
    @Getter
    private T entity;
    private int rowNo = 0;

    public final ExcelErrorHandler excelErrorHandler = new ExcelErrorHandler();

    private final ExcelMergeReadHandler readHandler = new ExcelMergeReadHandler() {
        @Override
        public void explainMergeData() {
//            List<CellExtra> mergeInfoList = this.getExtraMergeInfoList();
//            for (CellExtra cellExtra : mergeInfoList) {
//                Object value = data
//                        .get(cellExtra.getFirstRowIndex())
//                        .get(cellExtra.getFirstColumnIndex());
//                for (int i = cellExtra.getFirstColumnIndex() + 1; i <= cellExtra.getLastRowIndex(); i++) {
//                    for (int j = cellExtra.getFirstColumnIndex() + 1; j <= cellExtra.getLastColumnIndex(); j++) {
//                        data
//                                .get(i)
//                                .put(j,
//                                        value);
//                        System.out.println(data.get(i));
//                        System.out.println(data
//                                .get(i)
//                                .get(j));
//                    }
//                }
//            }
//            log.info(data.toString());
        }
    };

    public ExcelFormModelDataListener(Class<T> aClass) {
        this.aClass = aClass;
    }

    @Override
    public void invoke(Map<Integer, Object> data, AnalysisContext analysisContext) {
        this.data.put(rowNo,
                data);
        rowNo++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try {
            readHandler.merge();
            setValue();
            validate();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(),
                    e);
        }
    }

    private void validate() {
        /*required*/
        int defaultValueIndex = getDefaultValueIndex();
        for (Field field : aClass.getDeclaredFields()) {
            ExcelValidate excelValidate = field.getAnnotation(ExcelValidate.class);
            ExcelFormField excelFormField = field.getAnnotation(ExcelFormField.class);
            if (excelValidate != null && excelFormField != null) {
                Object value = BeanUtil.getFieldValue(entity,
                        field.getName());
                int columnNo = getColumnIndex(defaultValueIndex,
                        excelFormField) + 1;
                int rowNo = excelFormField.rowIndex() + 1;
                if (excelValidate.required()) {
                    if (value == null) {
                        excelErrorHandler.addErrorMessages("第%d行第%d列不能为空",
                                rowNo,
                                columnNo);
                    }
                }
                if (excelValidate.enums().length > 0 && value != null) {
                    boolean nonExist = Arrays
                            .stream(excelValidate.enums())
                            .noneMatch(s -> s.equals(value));
                    if (nonExist) {
                        excelErrorHandler.addErrorMessages("第%d行,第%d列值错误%s[%s]",
                                rowNo,
                                columnNo,
                                value,
                                String.join(",",
                                        excelValidate.enums()));
                    }
                }
            }
        }
    }

    private void setValue() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<T> constructor = aClass.getConstructor();
        entity = constructor.newInstance();
        int defaultValueIndex = getDefaultValueIndex();
        for (Field field : aClass.getDeclaredFields()) {
            ExcelFormField excelFormField = field.getAnnotation(ExcelFormField.class);
            if (excelFormField != null) {
                setValue(defaultValueIndex,
                        excelFormField,
                        field);
            }
        }
    }

    private int getDefaultValueIndex() {
        ExcelForm defaultExcelForm = aClass.getAnnotation(ExcelForm.class);
        return defaultExcelForm.value();
    }

    private void setValue(int defaultColumnIndex, ExcelFormField excelFormField, Field field) {
        int rowIndex = excelFormField.rowIndex();
        int columnIndex = getColumnIndex(defaultColumnIndex,
                excelFormField);
//        调试用
//        if (rowIndex == 14) {
//            System.out.println(1);
//        }
        Object value = data
                .get(rowIndex)
                .get(columnIndex);
        BeanUtil.setFieldValue(entity,
                field.getName(),
                value);
    }

    private int getColumnIndex(int defaultColumnIndex, ExcelFormField excelFormField) {
        int columnIndex = excelFormField.columnIndex();
        if (columnIndex == 0) {
            columnIndex = defaultColumnIndex;
        }
        return columnIndex;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        readHandler.addMergeData(extra);
    }
}
