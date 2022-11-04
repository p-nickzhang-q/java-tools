package zh.tools.word;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static zh.tools.word.WordFunctionParams.createXwpfRun;

@FunctionalInterface
public interface WordFillExport {

    String REGEX = "<<[\\u4e00-\\u9fa50-9A-Za-z]+>>";
    String EMPTY_STR = "";
    String VALUE_FORMAT = "<<%s>>";

    static ByteArrayInputStream toByteArrayInputStream(XWPFDocument xwpfDocument) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//二进制OutputStream
        xwpfDocument.write(byteArrayOutputStream);//文档写入流
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    default XWPFDocument run(HttpServletResponse response, String templateFileName, String fileName) throws IOException, InvalidFormatException {
        InputStream resourceAsStream = this
                .getClass()
                .getResourceAsStream(templateFileName);
        return process(response,
                fileName,
                resourceAsStream);
    }

    default XWPFDocument run(HttpServletResponse response, InputStream templateFile, String fileName) throws IOException {
        return process(response,
                fileName,
                templateFile);
    }

    default XWPFDocument process(HttpServletResponse response, String fileName, InputStream resourceAsStream) throws IOException {
        Util.setRes(response,
                fileName);
        ServletOutputStream outputStream = response.getOutputStream();
        assert resourceAsStream != null;
        return run(resourceAsStream,
                outputStream);
    }

    default XWPFDocument run(InputStream resourceAsStream, OutputStream outputStream) throws IOException {
        XWPFDocument xwpfDocument = new XWPFDocument(resourceAsStream);
        Map<String, Object> newMap = getNewValMap(data(),
                true);

        List<XWPFParagraph> parasList = xwpfDocument.getParagraphs();
        replaceInAllParagraphs(parasList,
                newMap);
        List<XWPFTable> tables = xwpfDocument.getTables();
        replaceInTables(tables,
                newMap);
        xwpfDocument.write(outputStream);
        outputStream.close();
        return xwpfDocument;
    }

    default Map<String, Object> getNewValMap(Map<String, Object> map, boolean valueProcess) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (valueProcess) {
                valueProcess(entry);
            }
            newMap.put(String.format(VALUE_FORMAT,
                    entry.getKey()),
                    entry.getValue());
        }
        return newMap;
    }

    default void valueProcess(Map.Entry<String, Object> entry) {

    }

    default void replaceInParagraph(XWPFParagraph xwpfParagraph, String pattern, Object value) {
        String allStr = getAllString(xwpfParagraph);
        CTRPr copyRPr = getCtrPr(xwpfParagraph);
        removeOldRuns(xwpfParagraph);
        WordFunctionParams wordFunctionParams;
        if (value instanceof WordFunctionParams) {
            wordFunctionParams = ((WordFunctionParams) value);
        } else {
            wordFunctionParams = WordFunctionParams
                    .create()
                    .add(WordFunctionParams.stringProcess,
                            Optional
                                    .ofNullable(value)
                                    .map(Object::toString)
                                    .orElse(""));
        }
        for (WordFunctionParams.WordFunctionParam param : wordFunctionParams.getParams()) {
            param
                    .getProcess()
                    .run(xwpfParagraph,
                            allStr,
                            pattern,
                            param.getValue(),
                            copyRPr);
        }
    }

    default void clearPlaceholder(XWPFParagraph xwpfParagraph, List<String> patternStrings) {
        String allStr = getAllString(xwpfParagraph);
        CTRPr copyRPr = getCtrPr(xwpfParagraph);
        removeOldRuns(xwpfParagraph);
        XWPFRun xwpfRun = createXwpfRun(xwpfParagraph,
                copyRPr);
        for (String s : patternStrings) {
            allStr = allStr.replace(s,
                    "");
        }
        xwpfRun.setText(allStr);
    }

    default CTRPr getCtrPr(XWPFParagraph xwpfParagraph) {
        CTRPr copyRPr = null;
        if (!xwpfParagraph
                .getRuns()
                .isEmpty()) {
            CTRPr rPr = xwpfParagraph
                    .getRuns()
                    .get(0)
                    .getCTR()
                    .getRPr();
            if (rPr != null) {
                copyRPr = (CTRPr) rPr.copy();
            }

        }
        return copyRPr;
    }

    default String getAllString(XWPFParagraph xwpfParagraph) {
        return clearSpan(xwpfParagraph
                .getRuns()
                .stream()
                .map(XWPFRun::text)
                .collect(Collectors.joining("")));
    }

    default void removeOldRuns(XWPFParagraph xwpfParagraph) {
        int size = xwpfParagraph
                .getRuns()
                .size();
        for (int i = size; i > 0; i--) {
            try {
                xwpfParagraph.removeRun(i - 1);
            } catch (Exception ignored) {

            }
        }
    }

    default Matcher matchRegex(String str) {
        Pattern pattern = Pattern.compile(REGEX);
        return pattern.matcher(str);
    }

    default void replaceInAllParagraphs(List<XWPFParagraph> xwpfParagraphList, Map<String, Object> params) {
        for (String key : params.keySet()) {
            for (XWPFParagraph paragraph : xwpfParagraphList) {
                if (paragraph.getText() == null || paragraph
                        .getText()
                        .equals(EMPTY_STR)) {
                    continue;
                }
                String text = clearSpan(paragraph.getText());
                /*值为集合,不在此方法渲染*/
                if (text.contains(key) && !(params.get(key) instanceof List)) {
                    replaceInParagraph(paragraph,
                            key,
                            params.get(key));
                }
            }
        }
        for (XWPFParagraph paragraph : xwpfParagraphList) {
            String text = clearSpan(paragraph.getText());
            /*没有更新的占位符,替换为空*/
            if (matchRegex(text)
                    .find()) {
                List<String> strings = new ArrayList<>();
                Matcher matcher = Pattern
                        .compile(REGEX)
                        .matcher(text);
                while (matcher.find()) {
                    strings.add(matcher.group());
                }
                clearPlaceholder(paragraph,
                        strings);
            }
        }
    }

    default String clearSpan(String text) {
        return text.trim();
    }

    default void replaceInTables(List<XWPFTable> xwpfTableList, Map<String, Object> params) {
        for (XWPFTable table : xwpfTableList) {
            replaceInTable(table,
                    params);
        }
    }

    default void replaceInTable(XWPFTable xwpfTable, Map<String, Object> params) {
        List<XWPFTableRow> rows = xwpfTable.getRows();
        replaceInRows(rows,
                params,
                xwpfTable);
    }

    default void replaceInRows(List<XWPFTableRow> rows, Map<String, Object> params, XWPFTable table) {

        BiConsumer<XWPFTableRow, Map<String, Object>> setRowValue = (tableRow, valueMap) -> {
            for (XWPFTableCell tableCell : tableRow.getTableCells()) {
                replaceInAllParagraphs(tableCell.getParagraphs(),
                        valueMap);
            }
        };

        Function<XWPFTableRow, XWPFTableRow> copyRow = (sourceRow) -> {
            XWPFTableRow copy = new XWPFTableRow((CTRow) sourceRow
                    .getCtRow()
                    .copy(),
                    table);
            copyValue(copy,
                    sourceRow);

            return copy;
        };

        params
                .entrySet()
                .stream()
                .filter(stringObjectEntry -> stringObjectEntry.getValue() instanceof List)
                .forEach(stringObjectEntry -> {
                    Supplier<Integer> getTemplateIndex = () -> {
                        for (int i = 0; i < table
                                .getRows()
                                .size(); i++) {
                            XWPFTableCell firstCell = rows
                                    .get(i)
                                    .getCell(0);
                            if (firstCell
                                    .getText()
                                    .contains(stringObjectEntry.getKey())) {
                                setCellText(firstCell,
                                        firstCell
                                                .getText()
                                                .replace(stringObjectEntry.getKey(),
                                                        ""));
                                return i;
                            }
                        }
                        return -1;
                    };
                    Integer templateIndex = getTemplateIndex.get();
                    for (int i = 0; i < rows.size(); i++) {
                        XWPFTableRow row = rows.get(i);
                        if (templateIndex == i) {
                            int finalI = i;
                            Runnable templateProcess = () -> {
                                List<?> values = (List<?>) stringObjectEntry.getValue();
                                for (Object value : values) {
                                    XWPFTableRow copy = copyRow.apply(row);
                                    Map<String, Object> childValueMap = getNewValMap((Map<String, Object>) value,
                                            false);
                                    setRowValue.accept(copy,
                                            childValueMap);
                                    table.addRow(copy);
                                }
                                table.removeRow(finalI);
                            };
                            templateProcess.run();
                        }
                    }
                });
        //填充普通属性
        for (XWPFTableRow row : rows) {
            replaceInCells(row.getTableCells(),
                    params);
        }
    }

    default void setCellText(XWPFTableCell cell, String value) {
        final XmlObject[] copyStyle = {null, null};
        cell
                .getParagraphs()
                .stream()
                .findAny()
                .ifPresent(xwpfParagraph -> {
                    CTPPr pPr = xwpfParagraph
                            .getCTP()
                            .getPPr();
                    if (pPr != null) {
                        copyStyle[0] = pPr.copy();
                    }
                    xwpfParagraph
                            .getRuns()
                            .stream()
                            .findAny()
                            .ifPresent(xwpfRun -> {
                                CTRPr rPr = xwpfRun
                                        .getCTR()
                                        .getRPr();
                                if (rPr != null) {
                                    copyStyle[1] = rPr.copy();
                                }
                            });
                });
        for (int i = 0; i < cell
                .getParagraphs()
                .size(); i++) {
            cell.removeParagraph(i);
        }
        XWPFParagraph xwpfParagraph = cell.addParagraph();
        if (copyStyle[0] != null) {
            xwpfParagraph
                    .getCTP()
                    .setPPr((CTPPr) copyStyle[0]);
        }
        XWPFRun xwpfRun = xwpfParagraph.insertNewRun(0);
        if (copyStyle[1] != null) {
            xwpfRun
                    .getCTR()
                    .setRPr((CTRPr) copyStyle[1]);
        }
        xwpfRun.setText(value);
    }

    default void copyValue(XWPFTableRow targetRow, XWPFTableRow sourceRow) {
        List<XWPFTableCell> tableCells = sourceRow.getTableCells();
        for (int i = 0; i < tableCells.size(); i++) {
            setCellText(targetRow.getCell(i),
                    tableCells
                            .get(i)
                            .getText());
        }
    }

    default void replaceInCells(List<XWPFTableCell> xwpfTableCellList, Map<String, Object> params) {
        for (XWPFTableCell cell : xwpfTableCellList) {
            replaceInCell(cell,
                    params);
        }
    }

    default void replaceInCell(XWPFTableCell cell, Map<String, Object> params) {
        List<XWPFParagraph> cellParagraphs = cell.getParagraphs();
        replaceInAllParagraphs(cellParagraphs,
                params);
    }

    Map<String, Object> data();

}
