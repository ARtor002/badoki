package ir.artor.badoki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BadokiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadokiApplication.class, args);
    }
}
