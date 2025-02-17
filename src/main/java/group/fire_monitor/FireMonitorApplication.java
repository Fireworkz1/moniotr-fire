package group.fire_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FireMonitorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FireMonitorApplication.class, args);

    }

}
