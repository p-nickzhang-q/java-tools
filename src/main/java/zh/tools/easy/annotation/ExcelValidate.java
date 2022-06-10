package zh.tools.easy.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelValidate {

  boolean required() default false;

  String[] enums() default {};

}
