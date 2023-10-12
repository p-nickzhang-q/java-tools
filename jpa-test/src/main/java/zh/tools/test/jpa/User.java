package zh.tools.test.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import zh.tools.jpa.BaseFilterService;
import zh.tools.jpa.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
@Data
public class User implements PersistentEntity<User, Long> {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @ManyToOne
    private Department department;

    @NotNull
    @Override
    public Class<? extends BaseFilterService<User, Long>> getClazz() {
        return UserService.class;
    }

    public enum Tag {
        vip, mobile, email, male, mac, superVip, lost
    }

    public User(String name, Integer age, Department department) {
        this.name = name;
        this.age = age;
        this.department = department;
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

    public User() {
    }
}
