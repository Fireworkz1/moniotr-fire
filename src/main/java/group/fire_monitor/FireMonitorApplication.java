package group.fire_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FireMonitorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FireMonitorApplication.class, args);

    }

}
