package com.flytbase.drone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Main application class for the Drone Survey Management System. */
@SpringBootApplication
@EnableScheduling
public class DroneSurveyApplication {

  public static void main(String[] args) {
    SpringApplication.run(DroneSurveyApplication.class, args);
  }
}
