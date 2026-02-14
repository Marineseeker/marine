package com.marine.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MarineApplication {
  public static void main(String[] args) {
    SpringApplication.run(MarineApplication.class, args);
  }
}
