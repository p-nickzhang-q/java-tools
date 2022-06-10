package zh.tools.easy.imports;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ExcelErrorHandler {
    @Getter
    protected List<String> errMsg = new ArrayList<>();

    public ExcelErrorHandler() {
    }

    public void addErrorMessages(String msg, Object... args) {
        errMsg.add(String.format(msg,
                args));
    }

}