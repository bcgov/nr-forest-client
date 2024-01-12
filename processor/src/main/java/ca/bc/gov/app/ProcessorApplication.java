package ca.bc.gov.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessorApplication.class, args);
  }

}
