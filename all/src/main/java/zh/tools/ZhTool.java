package zh.tools;

import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.ClassUtil;

import java.util.Set;

public class ZhTool {

    public static void main(String[] args) {
        Set<Class<?>> classes = ClassUtil.scanPackage("zh.tools");
        final ConsoleTable consoleTable = ConsoleTable
                .create()
                .addHeader("类名",
                        "所在包");
        for (Class<?> aClass : classes) {
            consoleTable.addBody(aClass.getSimpleName(),
                    aClass
                            .getPackage()
                            .getName());
        }
        consoleTable.print();
    }
}
