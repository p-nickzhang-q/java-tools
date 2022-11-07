package zh.tools.excel.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelFormField {
    /*值所在行的描述*/
    int rowIndex();

    int columnIndex() default 0;
}
