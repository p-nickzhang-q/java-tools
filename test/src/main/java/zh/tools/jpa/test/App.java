package zh.tools.jpa.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "zh.tools.jpa.test")
@EntityScan(basePackages = "zh.tools.jpa.test")
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,
                args);
    }
}
