package zh.tools.jpa.test;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class Department {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Department(String name) {
        this.name = name;
    }

    public Department() {
    }

    public static Department empty() {
        return new Department();
    }
}
