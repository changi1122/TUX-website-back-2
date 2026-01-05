package kr.ac.cbnu.tux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TuxWebsite2BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuxWebsite2BackApplication.class, args);
    }

}
