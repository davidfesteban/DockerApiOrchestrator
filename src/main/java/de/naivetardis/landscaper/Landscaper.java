package de.naivetardis.landscaper;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@AllArgsConstructor
@EnableScheduling
@ActiveProfiles
@EnableAspectJAutoProxy
@EnableAsync
@EnableWebMvc
public class Landscaper {
    public static void main(String[] args) {
        SpringApplication.run(Landscaper.class, args);
    }

}
