package zh.tools.excel.imports;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.RowTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import zh.tools.excel.annotation.ExcelValidate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public abstract class ExcelAnalysisEventListener<T> extends AnalysisEventListener<T> {

    public final ExcelErrorHandler excelErrorHandler = new ExcelErrorHandler();
    @Getter
    private Integer headRowNumber = 1;
    private Class<T> clazz;

    private final ExcelMergeReadHandler handler = new ExcelMergeReadHandler() {
        @Override
        public void explainMergeData() {
            /*循环所有合并单元格信息*/
            this
                    .getExtraMergeInfoList()
                    .forEach(cellExtra -> {
                        int firstRowIndex = cellExtra.getFirstRowIndex() - headRowNumber;
                        int lastRowIndex = cellExtra.getLastRowIndex() - headRowNumber;
                        int firstColumnIndex = cellExtra.getFirstColumnIndex();
                        int lastColumnIndex = cellExtra.getLastColumnIndex();
                        /*获取初始值*/
                        Object initValue = getInitValueFromList(firstRowIndex,
                                firstColumnIndex);
                        /*设置值*/
                        for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                            for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                                setInitValueToList(initValue,
                                        i,
                                        j);
                            }
                        }
                    });
        }
    };
    public static final Predicate<AnalysisContext> IS_EMPTY = context -> RowTypeEnum.EMPTY.equals(context.readRowHolder().getRowType());

    public ExcelAnalysisEventListener(Integer headRowNumber, Class<T> clazz) {
        this.headRowNumber = headRowNumber;
        this.clazz = clazz;
    }

    @Getter
    protected List<T> entities = new ArrayList<>();

    @SneakyThrows
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        handler.merge();
        validate();
    }

    private void setInitValueToList(Object filedValue, int rowIndex, int columnIndex) {
        T object = entities.get(rowIndex);
        Field[] declaredFields = object
                .getClass()
                .getDeclaredFields();
        for (int index = 0; index < declaredFields.length; index++) {
            Field field = declaredFields[index];
            //提升反射性能，关闭安全检查
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                if (index == columnIndex) {
                    try {
                        field.set(object,
                                filedValue);
                        break;
                    } catch (IllegalAccessException e) {
                        excelErrorHandler.addErrorMessages("第%d行，第%d列解析异常",
                                rowIndex,
                                columnIndex);
                    }
                }
            }
        }
    }

    private Object getInitValueFromList(int firstRowIndex, int firstColumnIndex) {
        Object filedValue = null;
        T object = entities.get(firstRowIndex);
        Field[] declaredFields = object
                .getClass()
                .getDeclaredFields();
        for (int index = 0; index < declaredFields.length; index++) {
            /*提升反射性能，关闭安全检查*/
            Field field = declaredFields[index];
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null && index == firstColumnIndex) {
                try {
                    filedValue = field.get(object);
                    break;
                } catch (IllegalAccessException e) {
                    excelErrorHandler.addErrorMessages("第%d行，第%d列解析异常",
                            firstRowIndex,
                            firstColumnIndex);
                }
            }
        }
        return filedValue;
    }

    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        this.entities.add(t);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            log.error("第{}行，第{}列解析异常",
                    getRowIndex(excelDataConvertException),
                    excelDataConvertException.getColumnIndex() + 1);
            excelErrorHandler.addErrorMessages("第%d行，第%d列解析异常, %s",
                    getRowIndex(excelDataConvertException),
                    excelDataConvertException.getColumnIndex() + 1,
                    exception.getMessage());
        }
    }

    private Integer getRowIndex(ExcelDataConvertException excelDataConvertException) {
        return excelDataConvertException.getRowIndex() + 1;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        if (extra.getRowIndex() >= headRowNumber) {
            handler.addMergeData(extra);
        }
    }

    protected void validate() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field[] fields = {};
        if (!entities.isEmpty()) {
            Class<?> aClass = entities
                    .get(0)
                    .getClass();
            fields = aClass.getDeclaredFields();
        }
        for (int i = 0; i < entities.size(); i++) {
            int rowNo = i + 1;
            T t = entities.get(i);
            for (Field field : fields) {
                ExcelValidate excelValidate = field.getAnnotation(ExcelValidate.class);
                ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
                if (excelProperty == null) {
                    continue;
                }
                String columnName = String.join("-",
                        excelProperty.value());
                Object property = BeanUtil.getProperty(t,
                        field.getName());
                if (excelValidate != null) {
                    //required
                    if (excelValidate.required()) {
                        if (property == null) {
                            excelErrorHandler.addErrorMessages("第%d行,%s不能为空",
                                    rowNo,
                                    String.join(",",
                                            columnName));
                        }
                    }
                    //enums
                    if (excelValidate.enums().length > 0) {
                        boolean nonExist = Arrays
                                .stream(excelValidate.enums())
                                .noneMatch(s -> s.equals(property));
                        if (nonExist && property != null) {
                            excelErrorHandler.addErrorMessages(String.format("第%d行,%s值错误%s[%s]",
                                    rowNo,
                                    columnName,
                                    property,
                                    String.join(",",
                                            excelValidate.enums())));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        try {
            /*验证Excel格式是否正确*/
            Map<Integer, String> indexNameMap = getIndexNameMap();
            for (Map.Entry<Integer, String> indexEntry : indexNameMap.entrySet()) {
                if (!headMap.containsValue(indexEntry.getValue())) {
                    excelErrorHandler.addErrorMessages("列[%s]不存在",
                            indexEntry.getValue());
                }
            }
        } catch (NoSuchFieldException e) {
            log.error(e.getMessage(),
                    e);
        }
        super.invokeHeadMap(headMap,
                context);
    }

    public Map<Integer, String> getIndexNameMap() throws NoSuchFieldException {
        Map<Integer, String> result = new HashMap<>();
        Field field;
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            field = clazz.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                String[] values = excelProperty.value();
                StringBuilder value = new StringBuilder();
                for (String v : values) {
                    value.append(v);
                }
                result.put(i,
                        value.toString());
            }
        }
        return result;
    }


}
