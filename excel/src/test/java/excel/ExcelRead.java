package excel;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.io.file.FileWriter;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import zh.tools.excel.imports.ExcelClassRead;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ExcelRead {

    public static void main(String[] args) throws IllegalAccessException {
        InputStream inputStream = ExcelRead.class.getResourceAsStream("../清洁屋.xlsx");
        List<CleanHouse> cleanHouses = ExcelClassRead.read(CleanHouse.class,
                inputStream);
        CleanHouse last_cleanHouse_info = new CleanHouse();
        for (CleanHouse cleanHouse : cleanHouses) {
            for (Field field : CleanHouse.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(cleanHouse) != null) {
                    field.set(last_cleanHouse_info,
                            field.get(cleanHouse));
                } else {
                    Object value = field.get(last_cleanHouse_info);
                    field.set(cleanHouse,
                            value);
                }
            }
        }
        List<CleanHouse> error = cleanHouses
                .stream()
                .filter(cleanHouse -> StringUtils.isEmpty(cleanHouse.getStreet()) || StringUtils.isEmpty(cleanHouse.getXq()) || StringUtils.isEmpty(cleanHouse.getSq()) || StringUtils.isEmpty(cleanHouse.getSummer_open_time()) || StringUtils.isEmpty(cleanHouse.getWinter_open_time()))
                .collect(Collectors.toList());
        if (!error.isEmpty()) {
            throw new ValidateException(error.toString());
        }
//        List<CleanHouse> finalCleanHouses = cleanHouses
//                .stream()
//                .collect(Collectors.groupingBy(cleanHouse -> String.format("%s-%s-%s-%s",
//                        cleanHouse.getStreet(),
//                        cleanHouse.getSq(),
//                        cleanHouse.getXq(),
//                        cleanHouse.getAddress())))
//                .values()
//                .stream()
//                .map(houses -> {
//                    CleanHouse cleanHouse = houses.get(0);
//                    cleanHouse.setTypes(houses
//                            .stream()
//                            .map(CleanHouse::getType)
//                            .collect(Collectors.joining(",")));
//                    return cleanHouse;
//                })
//                .collect(Collectors.toList());
        FileWriter writer = new FileWriter("./清洁屋.json");
        writer.write(JSON.toJSONString(cleanHouses));
        log.info("success");
//        InputStream inputStream = ExcelRead
//                .class
//                .getResourceAsStream("../test.xlsx");
//        List<TestExcel> objs = ExcelClassRead.read(TestExcel.class, inputStream);
//        log.info(objs.toString());
    }

}
