package zh.tools.test.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "zh.tools.test.jpa")
@EntityScan(basePackages = "zh.tools.test.jpa")
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,
                args);
    }
}
