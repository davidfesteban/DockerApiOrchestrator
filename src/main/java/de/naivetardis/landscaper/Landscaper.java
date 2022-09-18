package de.naivetardis.landscaper;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@AllArgsConstructor
@EnableScheduling
@ActiveProfiles
@EnableAspectJAutoProxy
@EnableAsync
public class Landscaper {
    public static void main(String[] args) {
        SpringApplication.run(Landscaper.class, args);
    }

}
