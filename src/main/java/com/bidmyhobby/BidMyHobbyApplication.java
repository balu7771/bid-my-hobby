package com.bidmyhobby;

import com.bidmyhobby.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class BidMyHobbyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidMyHobbyApplication.class, args);
    }

}
