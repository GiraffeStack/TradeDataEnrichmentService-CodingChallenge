package com.bensonhoi.tradeenricher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@Slf4j
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        log.info("Application initialising");
        run(App.class, args);
        log.info("Application started");
    }
}