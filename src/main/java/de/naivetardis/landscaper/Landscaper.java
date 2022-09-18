package de.naivetardis.landscaper;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@AllArgsConstructor
@EnableScheduling
@ActiveProfiles
//@EnableRetry
public class Landscaper {
    public static void main(String[] args) {
        SpringApplication.run(Landscaper.class, args);
    }

}
