package zh.tools.test.mybatisplus;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import zh.tools.mybatisplus.BaseEntity;

import java.time.LocalDate;

@Data
public class User implements BaseEntity<Long> {
    private Long id;
    private String name;
    private Integer age;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    public User() {
    }

    public User(String name, Integer age, LocalDate birthDate) {
        this.name = name;
        this.age = age;
        this.birthDate = birthDate;
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
